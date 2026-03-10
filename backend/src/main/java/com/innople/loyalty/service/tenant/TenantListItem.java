package com.innople.loyalty.service.tenant;

import java.util.UUID;

public record TenantListItem(
        UUID tenantId,
        String name
) {
}

