package com.innople.loyalty.scheduler;

import com.innople.loyalty.service.points.PointExpirationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointExpirationScheduler {
    private final PointExpirationBatchService pointExpirationBatchService;

    @Scheduled(cron = "${app.points.auto-expire.cron:0 5 * * * *}")
    public void runAutoExpire() {
        try {
            PointExpirationBatchService.BatchExpireResult result = pointExpirationBatchService.expireAllTenants(Instant.now());
            log.info("Point auto-expire batch finished. tenants={}, members={}, expiredPoints={}",
                    result.tenantCount(), result.processedMemberCount(), result.expiredPointAmount());
        } catch (Exception e) {
            log.error("Point auto-expire batch failed", e);
        }
    }
}
