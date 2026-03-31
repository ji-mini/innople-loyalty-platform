package com.innople.loyalty.service.stamp;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StampAutoRedeemBatchService {

    private final TenantRepository tenantRepository;
    private final StampService stampService;

    public AllTenantsResult runAllTenants() {
        int tenantCount = 0;
        int totalCoupons = 0;
        for (Tenant tenant : tenantRepository.findAll()) {
            UUID tid = tenant.getTenantId() != null ? tenant.getTenantId() : tenant.getId();
            TenantContext.setTenantId(tid);
            try {
                StampService.AutoRedeemBatchResult r = stampService.runAutoRedeemForCurrentTenant();
                tenantCount++;
                totalCoupons += r.couponsIssued();
            } finally {
                TenantContext.clear();
            }
        }
        return new AllTenantsResult(tenantCount, totalCoupons);
    }

    public record AllTenantsResult(int tenantCount, int totalCouponsIssued) {
    }
}
