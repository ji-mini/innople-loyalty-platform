package com.innople.loyalty.domain.batch;

/**
 * 배치 식별자(batch_name) 상수.
 * <p>batch_job_config / batch_execution_history 의 batch_name 컬럼 값으로 사용한다.</p>
 */
public final class BatchNames {

    /** 자동 탈퇴 배치: WITHDRAW_REQUESTED 상태에서 유예기간 경과 회원을 WITHDRAWN 으로 전환. */
    public static final String AUTO_WITHDRAWAL = "AUTO_WITHDRAWAL";

    private BatchNames() {
    }
}
