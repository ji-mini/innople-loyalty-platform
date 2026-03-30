package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.DashboardDtos;
import jakarta.validation.Valid;
import com.innople.loyalty.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardDtos.DashboardResponse get() {
        return dashboardService.getDashboard();
    }

    @GetMapping("/goals/current")
    public DashboardDtos.DashboardGoalResponse getCurrentGoal() {
        return dashboardService.getCurrentGoal();
    }

    @PutMapping("/goals/current")
    public DashboardDtos.DashboardGoalResponse upsertCurrentGoal(@Valid @RequestBody DashboardDtos.UpsertDashboardGoalRequest request) {
        return dashboardService.upsertCurrentGoal(
                request.targetNewMembers(),
                request.targetEarn(),
                request.targetUse()
        );
    }
}
