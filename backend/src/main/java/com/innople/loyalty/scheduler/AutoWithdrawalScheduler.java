package com.innople.loyalty.scheduler;

import com.innople.loyalty.service.batch.AutoWithdrawalBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 자동 탈퇴 배치 스케줄러. 매시 정각에 전 테넌트를 순회하며,
 * 각 테넌트의 batch_job_config(enabled=true) + run_hour 도달 + 오늘 미처리 조건을 만족할 때만 실행한다.
 * (테넌트 순회/컨텍스트 세팅/중복 실행 방지는 {@link AutoWithdrawalBatchService}가 담당.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoWithdrawalScheduler {

    private final AutoWithdrawalBatchService autoWithdrawalBatchService;

    @Scheduled(cron = "${app.batch.auto-withdrawal.cron:0 0 * * * *}")
    public void runAutoWithdrawal() {
        try {
            AutoWithdrawalBatchService.BatchSweepResult result =
                    autoWithdrawalBatchService.runAllTenantsScheduled(Instant.now());
            log.info("Auto-withdrawal batch finished. tenants={}, executed={}, withdrawn={}, errors={}",
                    result.tenantCount(), result.executedTenantCount(),
                    result.processedMemberCount(), result.errorMemberCount());
        } catch (Exception e) {
            log.error("Auto-withdrawal batch failed", e);
        }
    }
}
