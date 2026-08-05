package com.innople.loyalty.domain.batch;

/**
 * 배치(테넌트별) 1회 실행의 결과 상태.
 * <p>값 집합은 V32 마이그레이션의 named CHECK(ck_batch_exec_history_status)와 반드시 일치해야 한다.</p>
 * <ul>
 *     <li>{@code RUNNING}: 실행 중(finished_at NULL)</li>
 *     <li>{@code SUCCESS}: 전건 처리 성공(대상 0건 포함)</li>
 *     <li>{@code PARTIAL}: 일부 성공 + 일부 실패</li>
 *     <li>{@code FAILED}: 전건 실패</li>
 * </ul>
 */
public enum BatchExecutionStatus {
    RUNNING,
    SUCCESS,
    PARTIAL,
    FAILED
}
