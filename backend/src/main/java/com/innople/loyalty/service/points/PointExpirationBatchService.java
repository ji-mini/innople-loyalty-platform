package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.batch.BatchJobConfig;
import com.innople.loyalty.domain.batch.BatchNames;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.PointLotRepository;
import com.innople.loyalty.repository.TenantRepository;
import com.innople.loyalty.service.batch.BatchRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 포인트 자동 소멸 배치.
 *
 * <p>배치관리 프레임워크 경로: {@link BatchRunner} 로 디스패치된다
 * (config/catch-up/이력은 {@link com.innople.loyalty.service.batch.BatchDispatchService}).</p>
 *
 * <p>레거시 수동 API {@code POST /api/v1/admin/points/expire/run} 경로는
 * {@link #expireAllTenants(Instant)} 를 그대로 사용한다 (config 무시, 전 테넌트).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointExpirationBatchService implements BatchRunner {
    private static final String AUTO_EXPIRE_REASON = "만료일 경과에 따른 자동 소멸";

    private final TenantRepository tenantRepository;
    private final PointLotRepository pointLotRepository;
    private final PointService pointService;

    /** 레거시 {@link #expireAllTenants} 전용 락 (프레임워크 AtomicBoolean 과 별개). */
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public String batchName() {
        return BatchNames.POINT_EXPIRATION;
    }

    /**
     * 프레임워크 경로: 현재 테넌트만 처리. 회원별 try-catch 격리.
     * 만료 기준 시각(ref) = 실행 시각 그대로 ({@code expiresAt <= ref}).
     */
    @Override
    public RunResult execute(BatchJobConfig config, Instant now) {
        Instant ref = now != null ? now : Instant.now();
        MemberExpireAgg agg = expireMembers(ref);
        return new RunResult(agg.processedMemberCount(), agg.errorMemberCount(), agg.lastError());
    }

    /**
     * 레거시 수동 API: 전 테넌트 순회 (batch_job_config 무시).
     * PointAdminController {@code POST /expire/run} 전용 — 변경하지 않는다.
     */
    public BatchExpireResult expireAllTenants(Instant referenceAt) {
        Instant ref = referenceAt != null ? referenceAt : Instant.now();
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Point expiration batch is already running");
        }

        int tenantCount = 0;
        int processedMemberCount = 0;
        long expiredPointAmount = 0L;

        try {
            for (Tenant tenant : tenantRepository.findAll()) {
                TenantContext.setTenantId(tenant.getTenantId());
                try {
                    TenantExpireResult tenantResult = expireCurrentTenant(ref);
                    tenantCount++;
                    processedMemberCount += tenantResult.processedMemberCount();
                    expiredPointAmount += tenantResult.expiredPointAmount();
                } finally {
                    TenantContext.clear();
                }
            }
            return new BatchExpireResult(ref, tenantCount, processedMemberCount, expiredPointAmount);
        } finally {
            running.set(false);
        }
    }

    public TenantExpireResult expireCurrentTenant(Instant referenceAt) {
        Instant ref = referenceAt != null ? referenceAt : Instant.now();
        MemberExpireAgg agg = expireMembers(ref);
        return new TenantExpireResult(agg.processedMemberCount(), agg.expiredPointAmount());
    }

    private MemberExpireAgg expireMembers(Instant ref) {
        UUID tenantId = TenantContext.requireTenantId();
        List<UUID> memberIds = pointLotRepository.findDistinctMemberIdsWithExpirableLots(tenantId, ref);

        int processedMemberCount = 0;
        int errorMemberCount = 0;
        long expiredPointAmount = 0L;
        String lastError = null;

        for (UUID memberId : memberIds) {
            try {
                PointOperationResult result = pointService.autoExpire(
                        memberId, ref, AUTO_EXPIRE_REASON, null, null);
                if (result.amount() == 0L) {
                    continue;
                }
                processedMemberCount++;
                expiredPointAmount += Math.abs(result.amount());
            } catch (Exception e) {
                errorMemberCount++;
                lastError = e.getMessage();
                log.warn("Point auto-expire failed for member {} (tenant {}): {}",
                        memberId, tenantId, e.getMessage());
            }
        }

        return new MemberExpireAgg(
                processedMemberCount, errorMemberCount, expiredPointAmount, lastError);
    }

    private record MemberExpireAgg(
            int processedMemberCount,
            int errorMemberCount,
            long expiredPointAmount,
            String lastError
    ) {
    }

    public record BatchExpireResult(
            Instant referenceAt,
            int tenantCount,
            int processedMemberCount,
            long expiredPointAmount
    ) {
    }

    public record TenantExpireResult(
            int processedMemberCount,
            long expiredPointAmount
    ) {
    }
}
