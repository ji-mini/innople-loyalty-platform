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

@RestController
@RequestMapping("/api/v1/reports/members")
@RequiredArgsConstructor
public class MemberReportController {

    private final MemberReportService memberReportService;

    @GetMapping
    public MemberReportDtos.MemberReportResponse get(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return memberReportService.getReport(targetDate);
    }
}
