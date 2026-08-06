package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.controller.dto.AdminDashboardDtos;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.service.dashboard.AdminDashboardService;
import com.innople.loyalty.service.dashboard.DashboardPeriod;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 대시보드 조회 API. 전 엔드포인트 관리자 인증 가드(requireAtLeast OPERATOR)를 적용한다.
 */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminRoleResolver adminRoleResolver;
    private final AdminDashboardService adminDashboardService;

    /** 1단(오늘 현황) + 2단(포인트 부채) + 4단(분포/배치 상태). */
    @GetMapping("/overview")
    public AdminDashboardDtos.OverviewResponse getOverview(HttpServletRequest httpRequest) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.OPERATOR);
        return adminDashboardService.getOverview();
    }

    /** 3단 추이 차트. period=7D|30D|3M (기본 30D). */
    @GetMapping("/trends")
    public AdminDashboardDtos.TrendsResponse getTrends(
            @RequestParam(defaultValue = "30D") String period,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.OPERATOR);
        return adminDashboardService.getTrends(DashboardPeriod.from(period));
    }
}
