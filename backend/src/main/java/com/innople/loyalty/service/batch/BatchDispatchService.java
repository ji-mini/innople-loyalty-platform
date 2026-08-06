package com.innople.loyalty.service.batch;

import com.innople.loyalty.common.AppTimeZones;
import com.innople.loyalty.common.DateRangeUtils;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.batch.BatchExecutionHistory;
import com.innople.loyalty.domain.batch.BatchExecutionStatus;
import com.innople.loyalty.domain.batch.BatchJobConfig;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.BatchExecutionHistoryRepository;
import com.innople.loyalty.repository.BatchJobConfigRepository;
import com.innople.loyalty.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 배치 프레임워크 오케스트레이터.
 *
 * <p>등록된 {@link BatchRunner} 를 batch_name 으로 디스패치한다.
 * 테넌트 순회·config/catch-up 판정·실행이력·중복 실행 방지(배치별 AtomicBoolean)를 공통으로 담당한다.</p>
 */
@Slf4j
@Service
public class BatchDispatchService {

    private static final Set<BatchExecutionStatus> COMPLETED_STATUSES =
            Set.of(BatchExecutionStatus.SUCCESS, BatchExecutionStatus.PARTIAL);

    private final Map<String, BatchRunner> runnersByName;
    private final List<BatchRunner> runners;
    private final TenantRepository tenantRepository;
    private final BatchJobConfigRepository batchJobConfigRepository;
    private final BatchExecutionHistoryRepository batchExecutionHistoryRepository;

    /** 배치(batchName) 단위 실행 락. 동일 배치의 스케줄↔수동만 상호 배제하고, 서로 다른 배치는 독립 실행한다. */
    private final Map<String, AtomicBoolean> runningByBatchName;

    public BatchDispatchService(
            List<BatchRunner> runners,
            TenantRepository tenantRepository,
            BatchJobConfigRepository batchJobConfigRepository,
            BatchExecutionHistoryRepository batchExecutionHistoryRepository
    ) {
        // 등록 순서를 유지해 스케줄 스윕 시 동일한 순서로 runner 를 순회한다.
        this.runners = List.copyOf(runners);
        this.runnersByName = runners.stream().collect(Collectors.toMap(
                BatchRunner::batchName,
                Function.identity(),
                (a, b) -> {
                    throw new IllegalStateException(
                            "Duplicate BatchRunner for batchName=" + a.batchName());
                },
                LinkedHashMap::new
        ));
        this.runningByBatchName = runners.stream().collect(Collectors.toMap(
                BatchRunner::batchName,
                r -> new AtomicBoolean(false),
                (a, b) -> a,
                LinkedHashMap::new
        ));
        this.tenantRepository = tenantRepository;
        this.batchJobConfigRepository = batchJobConfigRepository;
        this.batchExecutionHistoryRepository = batchExecutionHistoryRepository;
    }

    // =====================================================================
    // 스케줄 실행: 등록된 runner(배치)별 락 → 전 테넌트 catch-up 판정
    // =====================================================================
    public BatchSweepResult runAllTenantsScheduled(Instant referenceAt) {
        Instant now = referenceAt != null ? referenceAt : Instant.now();
        List<Tenant> tenants = tenantRepository.findAll();
        int tenantCount = tenants.size();
        int executedCount = 0;
        int processedMemberCount = 0;
        int errorMemberCount = 0;

        // 배치별 락: 한 배치가 수동 실행 중이어도 다른 배치는 스윕을 진행한다.
        for (BatchRunner runner : runners) {
            String batchName = runner.batchName();
            AtomicBoolean lock = runningByBatchName.get(batchName);
            if (lock == null || !lock.compareAndSet(false, true)) {
                log.info("Batch {} skipped this tick: already running", batchName);
                continue;
            }
            try {
                for (Tenant tenant : tenants) {
                    TenantContext.setTenantId(tenant.getTenantId());
                    try {
                        BatchExecutionHistory history = runForCurrentTenantWithCatchUp(runner, now);
                        if (history != null) {
                            executedCount++;
                            processedMemberCount += history.getProcessedCount();
                            errorMemberCount += history.getErrorCount();
                        }
                    } catch (Exception e) {
                        log.error("Batch {} failed for tenant {}",
                                batchName, tenant.getTenantId(), e);
                    } finally {
                        TenantContext.clear();
                    }
                }
            } finally {
                lock.set(false);
            }
        }

        return new BatchSweepResult(tenantCount, executedCount, processedMemberCount, errorMemberCount);
    }

    private BatchExecutionHistory runForCurrentTenantWithCatchUp(BatchRunner runner, Instant now) {
        UUID tenantId = TenantContext.requireTenantId();
        String batchName = runner.batchName();
        BatchJobConfig config = batchJobConfigRepository
                .findByTenantIdAndBatchName(tenantId, batchName)
                .orElse(null);

        if (config == null) {
            log.debug("Batch skipped (no config) batch={} tenant={}", batchName, tenantId);
            return null;
        }
        if (!config.isEnabled()) {
            log.debug("Batch skipped (disabled) batch={} tenant={}", batchName, tenantId);
            return null;
        }

        ZonedDateTime nowKst = now.atZone(AppTimeZones.KST);
        int currentHour = nowKst.getHour();
        if (currentHour != config.getRunHour()) {
            log.debug("Batch skipped (run_hour mismatch current={} run={}) batch={} tenant={}",
                    currentHour, config.getRunHour(), batchName, tenantId);
            return null;
        }

        DateRangeUtils.Range today = DateRangeUtils.kstDayRange(nowKst.toLocalDate());
        boolean alreadyDone = batchExecutionHistoryRepository.existsCompletedInRange(
                tenantId, batchName, COMPLETED_STATUSES, today.start(), today.endExclusive());
        if (alreadyDone) {
            log.debug("Batch skipped (already completed today) batch={} tenant={}", batchName, tenantId);
            return null;
        }

        return executeWithHistory(runner, config, now);
    }

    // =====================================================================
    // 수동 실행: 현재 컨텍스트 테넌트를 catch-up 무시하고 강제 1회 실행
    // =====================================================================
    public BatchExecutionHistory runManualForCurrentTenant(String batchName) {
        AtomicBoolean lock = runningByBatchName.get(batchName);
        if (lock == null) {
            throw new BatchExceptions.BatchConfigNotFoundException("지원하지 않는 배치입니다: " + batchName);
        }
        if (!lock.compareAndSet(false, true)) {
            throw new BatchExceptions.BatchAlreadyRunningException("배치가 이미 실행 중입니다. 잠시 후 다시 시도해주세요.");
        }
        try {
            BatchRunner runner = runnersByName.get(batchName);
            if (runner == null) {
                throw new BatchExceptions.BatchConfigNotFoundException("지원하지 않는 배치입니다: " + batchName);
            }
            UUID tenantId = TenantContext.requireTenantId();
            BatchJobConfig config = batchJobConfigRepository
                    .findByTenantIdAndBatchName(tenantId, batchName)
                    .orElseThrow(() -> new BatchExceptions.BatchConfigNotFoundException(
                            "배치 설정을 찾을 수 없습니다: " + batchName));
            if (!config.isEnabled()) {
                throw new BatchExceptions.BatchDisabledException("비활성화된 배치는 수동 실행할 수 없습니다: " + batchName);
            }
            return executeWithHistory(runner, config, Instant.now());
        } finally {
            lock.set(false);
        }
    }

    // =====================================================================
    // 공통 실행 + 이력 기록
    // =====================================================================
    private BatchExecutionHistory executeWithHistory(BatchRunner runner, BatchJobConfig config, Instant now) {
        UUID tenantId = TenantContext.requireTenantId();
        String batchName = config.getBatchName();

        BatchExecutionHistory history = batchExecutionHistoryRepository.save(
                BatchExecutionHistory.start(tenantId, batchName));

        int processed = 0;
        int error = 0;
        String lastError = null;
        try {
            BatchRunner.RunResult result = runner.execute(config, now);
            processed = result.processedCount();
            error = result.errorCount();
            lastError = result.lastError();
        } catch (Exception e) {
            error = Math.max(error, 1);
            lastError = e.getMessage();
            log.error("Batch {} execute threw for tenant {}: {}", batchName, tenantId, e.getMessage(), e);
        }

        BatchExecutionStatus status = resolveStatus(processed, error);
        history.finish(status, processed, error, buildErrorSummary(error, lastError));
        BatchExecutionHistory finished = batchExecutionHistoryRepository.save(history);
        log.info("Batch executed batch={} tenant={} status={} processed={} error={}",
                batchName, tenantId, status, processed, error);
        return finished;
    }

    private BatchExecutionStatus resolveStatus(int processed, int error) {
        if (error == 0) {
            return BatchExecutionStatus.SUCCESS;
        }
        if (processed > 0) {
            return BatchExecutionStatus.PARTIAL;
        }
        return BatchExecutionStatus.FAILED;
    }

    private String buildErrorSummary(int error, String lastError) {
        if (error == 0) {
            return null;
        }
        String tail = (lastError == null || lastError.isBlank()) ? "알 수 없는 오류" : lastError;
        return "실패 %d건. 마지막 오류: %s".formatted(error, tail);
    }

    public record BatchSweepResult(
            int tenantCount,
            int executedCount,
            int processedMemberCount,
            int errorMemberCount
    ) {
        static BatchSweepResult skipped() {
            return new BatchSweepResult(0, 0, 0, 0);
        }
    }
}
