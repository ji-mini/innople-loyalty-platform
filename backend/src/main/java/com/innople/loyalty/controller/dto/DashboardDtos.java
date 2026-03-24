package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.points.PointEventType;

import java.time.Instant;
import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {
    }

    public record DashboardSummaryResponse(
            long thisMonthNewMembers,
            long thisMonthEarn,
            long thisMonthUse,
            long totalMembers,
            long totalPointBalance
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

    public record DashboardResponse(
            DashboardSummaryResponse summary,
            List<RecentPointActivityResponse> recentPoints,
            List<RecentAdminActionResponse> recentAdmins
    ) {
    }
}
