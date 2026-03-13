package com.innople.loyalty.service.report;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberReportDtos;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberReportServiceImpl implements MemberReportService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public MemberReportDtos.MemberReportResponse getReport(LocalDate date) {
        UUID tenantId = TenantContext.requireTenantId();

        long newSignups = memberRepository.countByTenantIdAndJoinedAt(tenantId, date);
        long dormant = memberRepository.countByTenantIdAndDormantAtBetween(tenantId, date, date);
        long withdrawRequested = memberRepository.countByTenantIdAndStatusCode(tenantId, MemberStatusCodes.LEGACY_WITHDRAW_REQUESTED);
        long withdrawn = memberRepository.countByTenantIdAndWithdrawnAtBetween(tenantId, date, date);
        long totalSignups = memberRepository.countByTenantIdAndJoinedAtLessThanEqual(tenantId, date);

        return new MemberReportDtos.MemberReportResponse(
                date,
                newSignups,
                dormant,
                withdrawRequested,
                withdrawn,
                totalSignups
        );
    }
}
