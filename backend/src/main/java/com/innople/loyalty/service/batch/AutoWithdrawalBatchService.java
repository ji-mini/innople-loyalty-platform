package com.innople.loyalty.service.batch;

import com.innople.loyalty.common.AppTimeZones;
import com.innople.loyalty.common.DateRangeUtils;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.batch.BatchExecutionHistory;
import com.innople.loyalty.domain.batch.BatchExecutionStatus;
import com.innople.loyalty.domain.batch.BatchJobConfig;
import com.innople.loyalty.domain.batch.BatchNames;
import com.innople.loyalty.domain.member.HistoryActorType;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.BatchExecutionHistoryRepository;
import com.innople.loyalty.repository.BatchJobConfigRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.TenantRepository;
import com.innople.loyalty.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 자동 탈퇴 배치 실행 엔진.
 *
 * <p>WITHDRAW_REQUESTED 상태에서 유예기간(threshold_days) 경과 회원을 {@link MemberService#withdraw}
 * (SYSTEM 경로)로 전환한다. 상태 직접 세팅은 하지 않는다.</p>
 *
 * <p>트랜잭션 경계: 배치 루프를 하나의 큰 트랜잭션으로 감싸지 않는다. 회원별 처리는 {@code withdraw(...)}가
 * 각자 독립 커밋하며(@Transactional), 한 회원의 실패가 다른 회원을 롤백시키지 않도록 try-catch 로 격리한다.
 * 이력 기록도 처리와 별개의 저장(각 save 가 독립 트랜잭션)으로 안전하게 남긴다.</p>
 *
 * <p>중복 실행 방지: PointExpirationBatchService 패턴을 답습해 단일 {@link AtomicBoolean} 락을
 * 스케줄 실행과 수동 실행이 공유한다. 스케줄과 수동이 겹쳐 이중 처리되지 않는다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoWithdrawalBatchService {

    static final String AUTO_WITHDRAWAL_REASON = "자동 탈퇴 (유예기간 경과)";
    // catch-up 판정: 오늘 KST 범위 안에 이미 완료(SUCCESS/PARTIAL)된 이력이 있으면 재실행하지 않는다.
    private static final Set<BatchExecutionStatus> COMPLETED_STATUSES =
            Set.of(BatchExecutionStatus.SUCCESS, BatchExecutionStatus.PARTIAL);

    private final TenantRepository tenantRepository;
    private final BatchJobConfigRepository batchJobConfigRepository;
    private final BatchExecutionHistoryRepository batchExecutionHistoryRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    // =====================================================================
    // 스케줄 실행: 전 테넌트 순회 + catch-up 판정
    // =====================================================================
    public BatchSweepResult runAllTenantsScheduled(Instant referenceAt) {
        Instant now = referenceAt != null ? referenceAt : Instant.now();
        if (!running.compareAndSet(false, true)) {
            log.info("Auto-withdrawal batch skipped this tick: already running");
            return BatchSweepResult.skipped();
        }

        int tenantCount = 0;
        int executedTenantCount = 0;
        int processedMemberCount = 0;
        int errorMemberCount = 0;

        try {
            for (Tenant tenant : tenantRepository.findAll()) {
                tenantCount++;
                TenantContext.setTenantId(tenant.getTenantId());
                try {
                    BatchExecutionHistory history = runForCurrentTenantWithCatchUp(now);
                    if (history != null) {
                        executedTenantCount++;
                        processedMemberCount += history.getProcessedCount();
                        errorMemberCount += history.getErrorCount();
                    }
                } catch (Exception e) {
                    // 한 테넌트 처리 실패가 다른 테넌트 순회를 막지 않도록 격리한다.
                    log.error("Auto-withdrawal batch failed for tenant {}", tenant.getTenantId(), e);
                } finally {
                    TenantContext.clear();
                }
            }
            return new BatchSweepResult(tenantCount, executedTenantCount, processedMemberCount, errorMemberCount);
        } finally {
            running.set(false);
        }
    }

    /**
     * 현재 컨텍스트 테넌트에 대해 config/캐치업 판정을 거쳐 실행한다.
     * @return 실제 실행됐으면 종료 이력, 스킵됐으면 null.
     */
    private BatchExecutionHistory runForCurrentTenantWithCatchUp(Instant now) {
        UUID tenantId = TenantContext.requireTenantId();
        BatchJobConfig config = batchJobConfigRepository
                .findByTenantIdAndBatchName(tenantId, BatchNames.AUTO_WITHDRAWAL)
                .orElse(null);

        if (config == null) {
            log.debug("Auto-withdrawal skipped (no config) tenant={}", tenantId);
            return null;
        }
        if (!config.isEnabled()) {
            log.debug("Auto-withdrawal skipped (disabled) tenant={}", tenantId);
            return null;
        }

        ZonedDateTime nowKst = now.atZone(AppTimeZones.KST);
        int currentHour = nowKst.getHour();
        if (currentHour < config.getRunHour()) {
            log.debug("Auto-withdrawal skipped (before run_hour {}<{}) tenant={}",
                    currentHour, config.getRunHour(), tenantId);
            return null;
        }

        DateRangeUtils.Range today = DateRangeUtils.kstDayRange(nowKst.toLocalDate());
        boolean alreadyDone = batchExecutionHistoryRepository.existsCompletedInRange(
                tenantId, BatchNames.AUTO_WITHDRAWAL, COMPLETED_STATUSES, today.start(), today.endExclusive());
        if (alreadyDone) {
            log.debug("Auto-withdrawal skipped (already completed today) tenant={}", tenantId);
            return null;
        }

        return executeTenant(config, now);
    }

    // =====================================================================
    // 수동 실행: 현재 컨텍스트 테넌트를 catch-up 무시하고 강제 1회 실행
    // =====================================================================
    public BatchExecutionHistory runManualForCurrentTenant(String batchName) {
        if (!running.compareAndSet(false, true)) {
            throw new BatchExceptions.BatchAlreadyRunningException("배치가 이미 실행 중입니다. 잠시 후 다시 시도해주세요.");
        }
        try {
            UUID tenantId = TenantContext.requireTenantId();
            BatchJobConfig config = batchJobConfigRepository
                    .findByTenantIdAndBatchName(tenantId, batchName)
                    .orElseThrow(() -> new BatchExceptions.BatchConfigNotFoundException(
                            "배치 설정을 찾을 수 없습니다: " + batchName));
            if (!config.isEnabled()) {
                throw new BatchExceptions.BatchDisabledException("비활성화된 배치는 수동 실행할 수 없습니다: " + batchName);
            }
            // 수동 실행은 run_hour / 오늘 이미 처리됨 판정을 건너뛰고 강제 실행한다.
            return executeTenant(config, Instant.now());
        } finally {
            running.set(false);
        }
    }

    // =====================================================================
    // 공통 실행 엔진 (스케줄/수동 공유)
    // =====================================================================
    private BatchExecutionHistory executeTenant(BatchJobConfig config, Instant now) {
        UUID tenantId = TenantContext.requireTenantId();
        String batchName = config.getBatchName();

        BatchExecutionHistory history = batchExecutionHistoryRepository.save(
                BatchExecutionHistory.start(tenantId, batchName));

        // 유예기간은 "시간량" 개념이므로 KST 날짜 변환 없이 순수 Instant 산술로 판정한다.
        Instant threshold = now.minus(config.getThresholdDays(), ChronoUnit.DAYS);
        LocalDate withdrawnDate = now.atZone(AppTimeZones.KST).toLocalDate();

        List<String> targetMemberNos = memberRepository.findWithdrawTargetMemberNos(
                tenantId, MemberStatusCodes.WITHDRAW_REQUESTED, threshold);

        int processed = 0;
        int error = 0;
        String lastError = null;

        for (String memberNo : targetMemberNos) {
            try {
                memberService.withdraw(
                        memberNo,
                        new MemberService.WithdrawCommand(withdrawnDate, AUTO_WITHDRAWAL_REASON),
                        null,
                        HistoryActorType.SYSTEM
                );
                processed++;
            } catch (Exception e) {
                error++;
                lastError = e.getMessage();
                log.warn("Auto-withdrawal failed for member {} (tenant {}): {}", memberNo, tenantId, e.getMessage());
            }
        }

        BatchExecutionStatus status = resolveStatus(processed, error);
        history.finish(status, processed, error, buildErrorSummary(error, lastError));
        BatchExecutionHistory finished = batchExecutionHistoryRepository.save(history);
        log.info("Auto-withdrawal executed tenant={} status={} processed={} error={}",
                tenantId, status, processed, error);
        return finished;
    }

    private BatchExecutionStatus resolveStatus(int processed, int error) {
        if (error == 0) {
            // 대상 0건 포함, 전건 성공.
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
            int executedTenantCount,
            int processedMemberCount,
            int errorMemberCount
    ) {
        static BatchSweepResult skipped() {
            return new BatchSweepResult(0, 0, 0, 0);
        }
    }
}
