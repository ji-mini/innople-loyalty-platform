package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.PointDtos;
import com.innople.loyalty.service.points.PointAccountReconciliationService;
import com.innople.loyalty.service.points.PointExpirationBatchService;
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

    @PostMapping("/expire/run")
    public PointDtos.ExpireBatchRunResponse runExpireBatch(@RequestBody(required = false) PointDtos.ExpireBatchRunRequest request) {
        PointExpirationBatchService.BatchExpireResult result = pointExpirationBatchService.expireAllTenants(
                request != null ? request.referenceAt() : null
        );
        return new PointDtos.ExpireBatchRunResponse(
                result.referenceAt(),
                result.tenantCount(),
                result.processedMemberCount(),
                result.expiredPointAmount()
        );
    }

    @PostMapping("/reconcile/member")
    public PointDtos.ReconcileBalanceResponse reconcileMember(@Valid @RequestBody PointDtos.ReconcileBalanceRequest request) {
        PointAccountReconciliationService.ReconcileResult result = pointAccountReconciliationService.reconcileMember(request.memberId());
        return new PointDtos.ReconcileBalanceResponse(
                result.memberId(),
                result.ledgerBalance(),
                result.previousBalance(),
                result.currentBalance(),
                result.corrected()
        );
    }

    @PostMapping("/reconcile")
    public PointDtos.ReconcileTenantBalancesResponse reconcileCurrentTenant() {
        PointAccountReconciliationService.TenantReconcileResult result = pointAccountReconciliationService.reconcileCurrentTenant();
        return new PointDtos.ReconcileTenantBalancesResponse(
                result.processedAccountCount(),
                result.correctedAccountCount()
        );
    }
}
