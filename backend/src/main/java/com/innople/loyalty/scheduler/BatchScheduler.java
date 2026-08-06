package com.innople.loyalty.scheduler;

import com.innople.loyalty.service.batch.BatchDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 배치관리 프레임워크 공용 스케줄러.
 * 매시 정각에 전 테넌트를 순회하며, 등록된 모든 {@link com.innople.loyalty.service.batch.BatchRunner}
 * 에 대해 config(enabled) + run_hour + 오늘 KST 미완료(catch-up) 조건을 만족할 때만 실행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchDispatchService batchDispatchService;

    @Scheduled(cron = "${app.batch.hourly.cron:0 0 * * * *}")
    public void runHourly() {
        try {
            BatchDispatchService.BatchSweepResult result =
                    batchDispatchService.runAllTenantsScheduled(Instant.now());
            log.info("Batch hourly sweep finished. tenants={}, executed={}, processed={}, errors={}",
                    result.tenantCount(), result.executedCount(),
                    result.processedMemberCount(), result.errorMemberCount());
        } catch (Exception e) {
            log.error("Batch hourly sweep failed", e);
        }
    }
}
