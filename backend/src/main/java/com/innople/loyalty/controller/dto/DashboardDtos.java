package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.Min;

import com.innople.loyalty.domain.points.PointEventType;

import java.time.Instant;
import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record DashboardSummaryResponse(
            long thisMonthNewMembers,
            long prevMonthNewMembers,
            long avgNewMembers,
            long thisMonthEarn,
            long prevMonthEarn,
            long avgEarn,
            long thisMonthUse,
            long prevMonthUse,
            long avgUse,
            long targetNewMembers,
            long targetEarn,
            long targetUse,
            long totalMembers,
            long totalPointBalance
    ) {
    }

    public record DashboardGoalResponse(
            int targetYear,
            int targetMonth,
            long targetNewMembers,
            long targetEarn,
            long targetUse
    ) {
    }

    public record UpsertDashboardGoalRequest(
            @Min(0) long targetNewMembers,
            @Min(0) long targetEarn,
            @Min(0) long targetUse
    ) {
    }

    public record RecentPointActivityResponse(
            String id,
            Instant createdAt,
            String memberNo,
            String brand,
            String type,
            long amount,
            String reason
    ) {
    }

    public record RecentAdminActionResponse(
            String id,
            Instant createdAt,
            String adminName,
            String action,
            String target
    ) {
    }

    public record TodayStatusResponse(
            long todayEarn,
            long todayUse,
            long todayNewMembers
    ) {
    }

    /** 잔여 포인트가 7일 이내에 소멸 예정인 롯의 합계 및 해당 회원 수 */
    public record ExpiringPointsSummaryResponse(
            long pointsExpiringWithin7Days,
            long membersWithExpiringLots
    ) {
    }

    public record DashboardResponse(
            DashboardSummaryResponse summary,
            TodayStatusResponse todayStatus,
            ExpiringPointsSummaryResponse expiringPoints,
            List<RecentPointActivityResponse> recentPoints,
            List<RecentAdminActionResponse> recentAdmins
    ) {
    }
}
