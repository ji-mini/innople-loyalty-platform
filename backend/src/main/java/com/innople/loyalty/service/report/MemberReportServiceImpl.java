package com.innople.loyalty.service.report;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberReportDtos;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberReportServiceImpl implements MemberReportService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public MemberReportDtos.MemberReportResponse getReport(LocalDate fromDate, LocalDate toDate, LocalDate totalAsOfDate) {
        UUID tenantId = TenantContext.requireTenantId();

        long newSignups = memberRepository.countByTenantIdAndJoinedAtBetween(tenantId, fromDate, toDate);
        long dormant = memberRepository.countByTenantIdAndDormantAtBetween(tenantId, fromDate, toDate);
        long withdrawRequested = memberRepository.countByTenantIdAndStatusCode(tenantId, MemberStatusCodes.LEGACY_WITHDRAW_REQUESTED);
        long withdrawn = memberRepository.countByTenantIdAndWithdrawnAtBetween(tenantId, fromDate, toDate);
        long totalMembers = memberRepository.countByTenantIdAndStatusCodeNot(tenantId, MemberStatusCodes.WITHDRAWN);

        return new MemberReportDtos.MemberReportResponse(
                fromDate,
                toDate,
                totalAsOfDate,
                newSignups,
                dormant,
                withdrawRequested,
                withdrawn,
                totalMembers
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MemberReportDtos.MonthlyTotalsResponse getMonthlyTotals(int year) {
        UUID tenantId = TenantContext.requireTenantId();
        List<MemberReportDtos.MonthlyTotalItem> items = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();
            long total = memberRepository.countActiveMembersAsOf(tenantId, endOfMonth);
            items.add(new MemberReportDtos.MonthlyTotalItem(month, total));
        }
        return new MemberReportDtos.MonthlyTotalsResponse(year, items);
    }
}
