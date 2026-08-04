package com.innople.loyalty.service.batch;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.batch.BatchExecutionHistory;
import com.innople.loyalty.domain.batch.BatchJobConfig;
import com.innople.loyalty.repository.BatchExecutionHistoryRepository;
import com.innople.loyalty.repository.BatchJobConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 배치 설정(batch_job_config) CRUD 및 실행 이력 조회. (관리자 화면용)
 * 모든 조회/변경은 현재 테넌트({@link TenantContext}) 스코프로 제한한다.
 */
@Service
@RequiredArgsConstructor
public class BatchJobConfigService {

    private final BatchJobConfigRepository batchJobConfigRepository;
    private final BatchExecutionHistoryRepository batchExecutionHistoryRepository;

    /** 배치 설정 + 해당 배치의 마지막 실행 시각(없으면 null)을 함께 담는 조회 뷰. */
    public record BatchJobConfigView(BatchJobConfig config, Instant lastExecutedAt) {
    }

    @Transactional(readOnly = true)
    public List<BatchJobConfigView> listConfigs() {
        UUID tenantId = TenantContext.requireTenantId();
        List<BatchJobConfig> configs = batchJobConfigRepository.findByTenantIdOrderByBatchNameAsc(tenantId);
        // batch_name → 최신 started_at 을 집계 1회로 구한 뒤 각 config 에 매핑한다(N+1 회피).
        Map<String, Instant> lastByBatchName = batchExecutionHistoryRepository
                .findLastStartedAtByTenantGrouped(tenantId).stream()
                .collect(Collectors.toMap(
                        BatchExecutionHistoryRepository.BatchNameLastStartedAt::getBatchName,
                        BatchExecutionHistoryRepository.BatchNameLastStartedAt::getLastStartedAt));
        return configs.stream()
                .map(c -> new BatchJobConfigView(c, lastByBatchName.get(c.getBatchName())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BatchJobConfigView getConfig(String batchName) {
        UUID tenantId = TenantContext.requireTenantId();
        BatchJobConfig config = batchJobConfigRepository.findByTenantIdAndBatchName(tenantId, batchName)
                .orElseThrow(() -> new BatchExceptions.BatchConfigNotFoundException(
                        "배치 설정을 찾을 수 없습니다: " + batchName));
        Instant lastExecutedAt = batchExecutionHistoryRepository.findLastStartedAt(tenantId, batchName);
        return new BatchJobConfigView(config, lastExecutedAt);
    }

    @Transactional
    public BatchJobConfigView createConfig(
            String batchName,
            boolean enabled,
            short runHour,
            int thresholdDays,
            UUID updatedBy
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        if (batchJobConfigRepository.existsByTenantIdAndBatchName(tenantId, batchName)) {
            throw new BatchExceptions.BatchConfigAlreadyExistsException(
                    "이미 존재하는 배치 설정입니다: " + batchName);
        }
        BatchJobConfig config = BatchJobConfig.create(batchName, enabled, runHour, thresholdDays, updatedBy);
        BatchJobConfig saved = batchJobConfigRepository.save(config);
        Instant lastExecutedAt = batchExecutionHistoryRepository.findLastStartedAt(tenantId, batchName);
        return new BatchJobConfigView(saved, lastExecutedAt);
    }

    @Transactional
    public BatchJobConfigView updateConfig(
            String batchName,
            boolean enabled,
            short runHour,
            int thresholdDays,
            UUID updatedBy
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        BatchJobConfig config = batchJobConfigRepository.findByTenantIdAndBatchName(tenantId, batchName)
                .orElseThrow(() -> new BatchExceptions.BatchConfigNotFoundException(
                        "배치 설정을 찾을 수 없습니다: " + batchName));
        config.update(enabled, runHour, thresholdDays, updatedBy);
        BatchJobConfig saved = batchJobConfigRepository.save(config);
        Instant lastExecutedAt = batchExecutionHistoryRepository.findLastStartedAt(tenantId, batchName);
        return new BatchJobConfigView(saved, lastExecutedAt);
    }

    @Transactional(readOnly = true)
    public Page<BatchExecutionHistory> listExecutions(String batchName, Pageable pageable) {
        UUID tenantId = TenantContext.requireTenantId();
        if (batchName == null || batchName.isBlank()) {
            return batchExecutionHistoryRepository.findByTenantId(tenantId, pageable);
        }
        return batchExecutionHistoryRepository.findByTenantIdAndBatchName(tenantId, batchName.trim(), pageable);
    }
}
