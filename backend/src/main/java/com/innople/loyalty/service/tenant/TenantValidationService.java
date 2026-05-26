package com.innople.loyalty.service.tenant;

import java.util.UUID;

public interface TenantValidationService {
    void requireExistingTenant(UUID tenantId);
}
