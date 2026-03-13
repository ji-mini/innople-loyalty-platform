package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.MemberReportDtos;
import com.innople.loyalty.service.report.MemberReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/reports/members")
@RequiredArgsConstructor
public class MemberReportController {

    private final MemberReportService memberReportService;

    @GetMapping
    public MemberReportDtos.MemberReportResponse get(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate totalAsOfDate
    ) {
        YearMonth now = YearMonth.now();
        LocalDate from = fromDate != null ? fromDate : now.atDay(1);
        LocalDate to = toDate != null ? toDate : now.atEndOfMonth();
        LocalDate totalDate = totalAsOfDate != null ? totalAsOfDate : LocalDate.now();
        return memberReportService.getReport(from, to, totalDate);
    }

    @GetMapping("/monthly-totals")
    public MemberReportDtos.MonthlyTotalsResponse getMonthlyTotals(
            @RequestParam(required = false) Integer year
    ) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return memberReportService.getMonthlyTotals(targetYear);
    }
}
