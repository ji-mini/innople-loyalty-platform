package com.innople.loyalty.controller.dto;

import java.time.LocalDate;

public final class MemberReportDtos {
    private MemberReportDtos() {
    }

    public record MemberReportResponse(
            LocalDate date,
            long newSignups,
            long dormant,
            long withdrawRequested,
            long withdrawn,
            long totalSignups
    ) {
    }
}
