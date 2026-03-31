package com.innople.loyalty.config;

import com.innople.loyalty.service.stamp.StampAutoRedeemBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * AUTO 스탬프 정책: 조건 충족 계정에 대한 쿠폰 자동 발급(전 테넌트 순회).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StampAutoRedeemScheduler {

    private final StampAutoRedeemBatchService stampAutoRedeemBatchService;

    @Scheduled(cron = "${app.stamp.auto-redeem-cron:0 0 * * * *}")
    public void runHourly() {
        try {
            StampAutoRedeemBatchService.AllTenantsResult r = stampAutoRedeemBatchService.runAllTenants();
            log.info("Stamp auto-redeem batch: tenants={}, couponsIssued={}", r.tenantCount(), r.totalCouponsIssued());
        } catch (Exception e) {
            log.error("Stamp auto-redeem batch failed", e);
        }
    }
}
