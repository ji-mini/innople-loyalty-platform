package com.innople.loyalty.service.dashboard;

import com.innople.loyalty.controller.dto.AdminDashboardDtos;

public interface AdminDashboardService {

    /** 1단(오늘 현황) + 2단(포인트 부채) + 4단(분포/배치 상태). */
    AdminDashboardDtos.OverviewResponse getOverview();

    /** 3단(추이 차트) 기간별 일자 시계열. */
    AdminDashboardDtos.TrendsResponse getTrends(DashboardPeriod period);
}
