package com.innople.loyalty.service.tenant;

import java.time.Instant;
import java.util.UUID;

public interface TenantAdminService {
    TenantDetail get(UUID tenantId);

    TenantDetail update(UUID tenantId, String name);

    record TenantDetail(
            UUID tenantId,
            String name,
            String representativeCode,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

