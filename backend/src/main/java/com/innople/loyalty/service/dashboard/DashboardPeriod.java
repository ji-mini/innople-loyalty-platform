package com.innople.loyalty.service.dashboard;

import com.innople.loyalty.common.DateRangeUtils;

import java.time.LocalDate;
import java.util.Locale;

/**
 * 대시보드 추이 차트 기간 토글. 경계는 항상 {@link DateRangeUtils} 를 경유해 KST half-open 구간으로 산출한다.
 */
public enum DashboardPeriod {

    /** 오늘 포함 최근 7일. */
    D7("7D"),
    /** 오늘 포함 최근 30일. */
    D30("30D"),
    /** 오늘 포함 최근 3개월. */
    M3("3M");

    private final String code;

    DashboardPeriod(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static DashboardPeriod from(String raw) {
        if (raw == null || raw.isBlank()) {
            return D30;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (DashboardPeriod period : values()) {
            if (period.code.equals(normalized) || period.name().equals(normalized)) {
                return period;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 기간입니다: " + raw);
    }

    /** 오늘(KST 기준일)을 마지막 날로 포함하는 시작 일자. */
    public LocalDate startDate(LocalDate today) {
        return switch (this) {
            case D7 -> today.minusDays(6);
            case D30 -> today.minusDays(29);
            case M3 -> today.minusMonths(3).plusDays(1);
        };
    }

    /** [시작일 00:00 KST, 오늘+1일 00:00 KST) half-open 구간. */
    public DateRangeUtils.Range range(LocalDate today) {
        return DateRangeUtils.kstRange(startDate(today), today);
    }
}
