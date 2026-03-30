package com.innople.loyalty.service.dashboard;

import com.innople.loyalty.controller.dto.DashboardDtos;

public interface DashboardService {
    DashboardDtos.DashboardResponse getDashboard();

    DashboardDtos.DashboardGoalResponse getCurrentGoal();

    DashboardDtos.DashboardGoalResponse upsertCurrentGoal(long targetNewMembers, long targetEarn, long targetUse);
}
