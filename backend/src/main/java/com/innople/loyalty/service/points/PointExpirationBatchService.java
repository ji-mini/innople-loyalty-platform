package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.PointLotRepository;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class PointExpirationBatchService {
    private static final String AUTO_EXPIRE_REASON = "만료일 경과에 따른 자동 소멸";

    private final TenantRepository tenantRepository;
    private final PointLotRepository pointLotRepository;
    private final PointService pointService;

    private final AtomicBoolean running = new AtomicBoolean(false);

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
        UUID tenantId = TenantContext.requireTenantId();
        Instant ref = referenceAt != null ? referenceAt : Instant.now();
        List<UUID> memberIds = pointLotRepository.findDistinctMemberIdsWithExpirableLots(tenantId, ref);

        int processedMemberCount = 0;
        long expiredPointAmount = 0L;

        for (UUID memberId : memberIds) {
            PointOperationResult result = pointService.autoExpire(memberId, ref, AUTO_EXPIRE_REASON, null, null);
            if (result.amount() == 0L) {
                continue;
            }
            processedMemberCount++;
            expiredPointAmount += Math.abs(result.amount());
        }

        return new TenantExpireResult(processedMemberCount, expiredPointAmount);
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
