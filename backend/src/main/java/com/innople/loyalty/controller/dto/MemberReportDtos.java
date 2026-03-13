package com.innople.loyalty.controller.dto;

import java.time.LocalDate;
import java.util.List;

public final class MemberReportDtos {
    private MemberReportDtos() {
    }

    public record MemberReportResponse(
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate totalAsOfDate,
            long newSignups,
            long dormant,
            long withdrawRequested,
            long withdrawn,
            long totalMembers
    ) {
    }

    public record MonthlyTotalItem(int month, long totalMembers) {
    }

    public record MonthlyTotalsResponse(int year, List<MonthlyTotalItem> items) {
    }
}
