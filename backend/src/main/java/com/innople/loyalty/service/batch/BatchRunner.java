package com.innople.loyalty.service.batch;

import com.innople.loyalty.domain.batch.BatchJobConfig;

import java.time.Instant;

/**
 * 배치 종류별 실행 엔진. {@link BatchDispatchService}가 batch_name 으로 디스패치한다.
 *
 * <p>구현체는 현재 {@link com.innople.loyalty.config.TenantContext} 테넌트에 대해
 * 비즈니스 처리만 수행한다. config 조회·catch-up·실행이력·중복 락은 디스패처 책임이다.</p>
 */
public interface BatchRunner {

    /** {@link com.innople.loyalty.domain.batch.BatchNames} 상수와 동일한 batch_name. */
    String batchName();

    /**
     * 현재 테넌트에 대해 배치 본문을 실행한다.
     *
     * @param config 테넌트 배치 설정 (enabled=true 보장된 상태로 전달)
     * @param now    실행 기준 시각
     * @return 처리/실패 건수 집계 (이력 finish 에 사용)
     */
    RunResult execute(BatchJobConfig config, Instant now);

    record RunResult(int processedCount, int errorCount, String lastError) {
        public static RunResult empty() {
            return new RunResult(0, 0, null);
        }
    }
}
