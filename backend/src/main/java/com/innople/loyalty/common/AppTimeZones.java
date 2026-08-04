package com.innople.loyalty.common;

import java.time.ZoneId;

/**
 * 프로젝트 전역에서 사용하는 시간대(ZoneId) 공용 상수.
 *
 * <p>저장은 UTC(Instant/timestamptz)로 유지하되, "하루/날짜" 경계와 무인자 now() 해석은 항상
 * KST(Asia/Seoul) 기준으로 통일한다. 시각 처리 규칙이 KST/UTC/JVM기본으로 흩어지지 않도록
 * 모든 KST 참조는 이 상수를 사용한다.</p>
 *
 * <p>현재는 단일 KST 테넌트 전제로 ZoneId 를 하드코딩한다. 다국가 테넌트 지원 시
 * 테넌트별 ZoneId 주입으로 리팩터링한다.</p>
 */
public final class AppTimeZones {

    /** 서비스 표준 시간대: 한국 표준시(KST). */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private AppTimeZones() {
    }
}
