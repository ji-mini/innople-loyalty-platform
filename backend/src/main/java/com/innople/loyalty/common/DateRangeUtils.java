package com.innople.loyalty.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 날짜(LocalDate)를 KST(Asia/Seoul) 기준 half-open Instant 구간으로 변환하는 유틸.
 *
 * <p>저장은 UTC(Instant/timestamptz)로 유지하되, 관리자 화면에서 다루는 "하루"의 경계는 KST 기준으로 계산한다.
 * 구간은 항상 half-open [start, endExclusive) 이며 쿼리 조건은 {@code field >= start AND field < endExclusive} 로 사용한다.
 * (inclusive-end 방식의 마이크로초 정밀도 누락/중복을 방지)</p>
 *
 * <p>시간대는 프로젝트 공용 상수 {@link AppTimeZones#KST} 를 사용한다(단일 KST 테넌트 전제).</p>
 */
public final class DateRangeUtils {

    private static final ZoneId KST = AppTimeZones.KST;

    private DateRangeUtils() {
    }

    /** 해당 일자의 KST 자정(시작) Instant. */
    public static Instant kstStartOfDay(LocalDate date) {
        return date.atStartOfDay(KST).toInstant();
    }

    /** 다음 날 KST 자정 Instant. 해당 일자를 "끝까지 포함"하는 exclusive 종료 경계로 사용한다. */
    public static Instant kstStartOfNextDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay(KST).toInstant();
    }

    /** from~to(양끝 날짜 포함)를 KST half-open 구간 [from 00:00, (to+1) 00:00) 으로 변환. */
    public static Range kstRange(LocalDate from, LocalDate to) {
        return new Range(kstStartOfDay(from), kstStartOfNextDay(to));
    }

    /** 단일 날짜의 KST half-open 구간 [date 00:00, (date+1) 00:00). */
    public static Range kstDayRange(LocalDate date) {
        return new Range(kstStartOfDay(date), kstStartOfNextDay(date));
    }

    /**
     * 기준일 기준 이번 달 1일 ~ 기준일(포함) KST half-open 구간.
     * 예: 8/6 → [8/1 00:00, 8/7 00:00).
     */
    public static Range kstMonthToDateRange(LocalDate today) {
        return kstRange(today.withDayOfMonth(1), today);
    }

    /**
     * 기준일 기준 전월 동기 구간: 전월 1일 ~ 전월 동일 일자(포함).
     * {@link LocalDate#minusMonths(long)} 가 말일을 클램프하므로 3/31 → 2/1~2/28(또는 2/29) 이 된다.
     * 예: 8/6 → [7/1 00:00, 7/7 00:00).
     */
    public static Range kstPreviousMonthToDateRange(LocalDate today) {
        LocalDate previousMonthSameDay = today.minusMonths(1);
        return kstRange(previousMonthSameDay.withDayOfMonth(1), previousMonthSameDay);
    }

    /**
     * KST 기준 half-open 구간. 쿼리 조건: {@code field >= start AND field < endExclusive}.
     */
    public record Range(Instant start, Instant endExclusive) {
    }
}
