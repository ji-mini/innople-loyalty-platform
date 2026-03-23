package com.innople.loyalty.service.tenant;

import java.time.Instant;
import java.util.UUID;

public interface TenantAdminService {
    TenantDetail create(String name, String representativeCode);

    TenantDetail get(UUID tenantId);

    TenantDetail update(UUID tenantId, String name, String representativeCode);

    void delete(UUID tenantId);

    record TenantDetail(
            UUID tenantId,
            String name,
            String representativeCode,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

