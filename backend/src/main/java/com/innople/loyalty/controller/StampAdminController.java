package com.innople.loyalty.controller;

import com.innople.loyalty.config.ApiAuditLogInterceptor;
import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.StampLedgerRepository;
import com.innople.loyalty.service.stamp.StampAutoRedeemBatchService;
import com.innople.loyalty.service.stamp.StampService;
import com.innople.loyalty.config.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/stamps")
@RequiredArgsConstructor
public class StampAdminController {

    private final StampService stampService;
    private final StampLedgerRepository stampLedgerRepository;
    private final MemberRepository memberRepository;
    private final StampAutoRedeemBatchService stampAutoRedeemBatchService;

    @PostMapping("/manual-grant")
    public StampDtos.StampManualGrantResponse manualGrant(
            @Valid @RequestBody StampDtos.StampManualGrantRequest request,
            HttpServletRequest httpRequest
    ) {
        StampService.ManualGrantResult result = stampService.grantManual(request.memberId(), request.stamps(), request.reason());
        String memberNo = memberRepository
                .findByTenantIdAndId(TenantContext.requireTenantId(), request.memberId())
                .map(m -> m.getMemberNo())
                .orElse("-");
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "스탬프 수기 지급 (회원 %s, +%d개)".formatted(memberNo, request.stamps())
        );
        return new StampDtos.StampManualGrantResponse(result.ledgerId(), result.currentBalance());
    }

    @GetMapping("/ledgers")
    public List<StampDtos.StampLedgerRow> ledgers(
            @RequestParam(required = false) String memberNo,
            @RequestParam(defaultValue = "200") int limit
    ) {
        int size = Math.min(Math.max(limit, 1), 500);
        return stampLedgerRepository.findLedgersForTenant(
                TenantContext.requireTenantId(),
                memberNo != null && !memberNo.isBlank() ? memberNo.trim() : null,
                PageRequest.of(0, size)
        );
    }

    @PostMapping("/batch/auto-redeem")
    public StampDtos.StampAutoRedeemBatchResponse runAutoRedeemCurrentTenant(HttpServletRequest httpRequest) {
        StampService.AutoRedeemBatchResult r = stampService.runAutoRedeemForCurrentTenant();
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "스탬프 AUTO 쿠폰 배치 (계정 %d건, 쿠폰 %d건)".formatted(r.accountsProcessed(), r.couponsIssued())
        );
        return new StampDtos.StampAutoRedeemBatchResponse(r.accountsProcessed(), r.couponsIssued());
    }

    @PostMapping("/batch/auto-redeem/all-tenants")
    public StampAutoRedeemBatchService.AllTenantsResult runAutoRedeemAllTenants(HttpServletRequest httpRequest) {
        StampAutoRedeemBatchService.AllTenantsResult r = stampAutoRedeemBatchService.runAllTenants();
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "스탬프 AUTO 쿠폰 전체 테넌트 배치 (테넌트 %d건, 쿠폰 %d건)".formatted(r.tenantCount(), r.totalCouponsIssued())
        );
        return r;
    }
}
