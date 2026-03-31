package com.innople.loyalty.domain.stamp;

public enum StampIssuanceMode {
    /** 기준 충족 시 자동 쿠폰 발급(스탬프 차감) */
    AUTO,
    /** 고객이 직접 받기 전까지 스탬프 유지 */
    MANUAL
}
