package com.innople.loyalty.service.report;

import com.innople.loyalty.common.DateRangeUtils;
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

        // 일시 컬럼(timestamptz)을 KST 기준 날짜 범위 [fromDate 00:00 KST, (toDate+1) 00:00 KST) half-open 으로 집계한다.
        DateRangeUtils.Range range = DateRangeUtils.kstRange(fromDate, toDate);

        long newSignups = memberRepository.countByTenantIdAndJoinedAtGreaterThanEqualAndJoinedAtLessThan(tenantId, range.start(), range.endExclusive());
        long dormant = memberRepository.countByTenantIdAndDormantAtGreaterThanEqualAndDormantAtLessThan(tenantId, range.start(), range.endExclusive());
        long withdrawRequested = memberRepository.countByTenantIdAndStatusCode(tenantId, MemberStatusCodes.WITHDRAW_REQUESTED);
        long withdrawn = memberRepository.countByTenantIdAndWithdrawnAtGreaterThanEqualAndWithdrawnAtLessThan(tenantId, range.start(), range.endExclusive());
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
            // "해당 월 말일 끝까지 활성" 기준: 말일 다음 날 KST 자정을 exclusive 경계로 사용한다.
            long total = memberRepository.countActiveMembersAsOf(tenantId, DateRangeUtils.kstStartOfNextDay(endOfMonth));
            items.add(new MemberReportDtos.MonthlyTotalItem(month, total));
        }
        return new MemberReportDtos.MonthlyTotalsResponse(year, items);
    }
}
