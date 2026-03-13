package com.innople.loyalty.service.report;

import com.innople.loyalty.controller.dto.MemberReportDtos;

import java.time.LocalDate;

public interface MemberReportService {

    MemberReportDtos.MemberReportResponse getReport(LocalDate date);
}
