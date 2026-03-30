package com.innople.loyalty.controller;

import com.innople.loyalty.config.ApiAuditLogInterceptor;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.PointDtos;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.service.points.PointAccountReconciliationService;
import com.innople.loyalty.service.points.PointExpirationBatchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/points")
@RequiredArgsConstructor
public class PointAdminController {
    private final PointExpirationBatchService pointExpirationBatchService;
    private final PointAccountReconciliationService pointAccountReconciliationService;
    private final MemberRepository memberRepository;

    @PostMapping("/expire/run")
    public PointDtos.ExpireBatchRunResponse runExpireBatch(
            @RequestBody(required = false) PointDtos.ExpireBatchRunRequest request,
            HttpServletRequest httpRequest
    ) {
        PointExpirationBatchService.BatchExpireResult result = pointExpirationBatchService.expireAllTenants(
                request != null ? request.referenceAt() : null
        );
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "만료 배치 실행 (테넌트 %d건, 회원 %d건, 소멸 %dP)".formatted(
                        result.tenantCount(), result.processedMemberCount(), result.expiredPointAmount())
        );
        return new PointDtos.ExpireBatchRunResponse(
                result.referenceAt(),
                result.tenantCount(),
                result.processedMemberCount(),
                result.expiredPointAmount()
        );
    }

    @PostMapping("/reconcile/member")
    public PointDtos.ReconcileBalanceResponse reconcileMember(@Valid @RequestBody PointDtos.ReconcileBalanceRequest request, HttpServletRequest httpRequest) {
        PointAccountReconciliationService.ReconcileResult result = pointAccountReconciliationService.reconcileMember(request.memberId());
        String memberNo = memberRepository
                .findByTenantIdAndId(TenantContext.requireTenantId(), request.memberId())
                .map(m -> m.getMemberNo())
                .orElse("알 수 없음");
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "회원 포인트 잔액 정합 (%s, 교정 %s)".formatted(memberNo, result.corrected() ? "있음" : "없음")
        );
        return new PointDtos.ReconcileBalanceResponse(
                result.memberId(),
                result.ledgerBalance(),
                result.previousBalance(),
                result.currentBalance(),
                result.corrected()
        );
    }

    @PostMapping("/reconcile")
    public PointDtos.ReconcileTenantBalancesResponse reconcileCurrentTenant(HttpServletRequest httpRequest) {
        PointAccountReconciliationService.TenantReconcileResult result = pointAccountReconciliationService.reconcileCurrentTenant();
        ApiAuditLogInterceptor.setAuditMessage(
                httpRequest,
                "테넌트 포인트 잔액 정합 (계정 %d건, 교정 %d건)".formatted(
                        result.processedAccountCount(), result.correctedAccountCount())
        );
        return new PointDtos.ReconcileTenantBalancesResponse(
                result.processedAccountCount(),
                result.correctedAccountCount()
        );
    }
}
