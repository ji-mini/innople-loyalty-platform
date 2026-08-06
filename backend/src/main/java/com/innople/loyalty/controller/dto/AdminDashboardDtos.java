package com.innople.loyalty.controller.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 관리자 대시보드 응답 DTO. 조회 전용이며 엔티티를 그대로 노출하지 않는다.
 */
public final class AdminDashboardDtos {

    private AdminDashboardDtos() {
    }

    /**
     * 현재 구간 값과 비교 구간 값. delta = current - previous.
     * <p>비교 구간은 지표마다 다르다(전월 동기 또는 전일).</p>
     */
    public record MetricValue(
            long current,
            long previous,
            long delta
    ) {
        public static MetricValue of(long current, long previous) {
            return new MetricValue(current, previous, current - previous);
        }
    }

    /**
     * 1단·2단 보조 KPI.
     * <ul>
     *   <li>newMembers / withdrawals: 이번 달(1일~기준일) vs 전월 동기</li>
     *   <li>activeMembers: 누적 활성 회원 vs 전일 동일 시각</li>
     *   <li>earnedPoints / usedPoints: 이번 달(1일~기준일) 집계. previous 는 사용하지 않음(0)</li>
     * </ul>
     */
    public record TodayKpiResponse(
            MetricValue newMembers,
            MetricValue withdrawals,
            MetricValue activeMembers,
            MetricValue earnedPoints,
            MetricValue usedPoints
    ) {
    }

    /** 2단: 누적 적립/사용 및 잔액·소멸 예정. */
    public record PointLiabilityResponse(
            long totalEarned,
            long totalUsed,
            long outstandingBalance,
            long expiringThisMonth
    ) {
    }

    /** 4단: 분포 항목. */
    public record DistributionItem(
            String key,
            String label,
            long count
    ) {
    }

    /** 4단: 배치 상태 요약. 실행 이력이 없으면 status=null. */
    public record BatchStatusItem(
            String batchName,
            boolean enabled,
            String status,
            Instant startedAt,
            Instant finishedAt,
            int processedCount,
            int errorCount,
            String errorMessage
    ) {
    }

    public record OverviewResponse(
            LocalDate baseDate,
            TodayKpiResponse today,
            PointLiabilityResponse pointLiability,
            List<DistributionItem> gradeDistribution,
            List<DistributionItem> statusDistribution,
            List<BatchStatusItem> batches
    ) {
    }

    /** 3단: 일자별 추이 한 점. 회원 차트와 포인트 차트가 동일 x축을 공유한다. */
    public record TrendPoint(
            LocalDate date,
            long signups,
            long withdrawals,
            long earnedPoints,
            long usedPoints
    ) {
    }

    public record TrendsResponse(
            String period,
            LocalDate fromDate,
            LocalDate toDate,
            List<TrendPoint> points
    ) {
    }
}
