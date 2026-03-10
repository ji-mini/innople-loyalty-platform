package com.innople.loyalty.controller.dto;

import java.util.List;
import java.util.UUID;

public final class TenantPublicDtos {
    private TenantPublicDtos() {
    }

    public record TenantItem(
            UUID tenantId,
            String name
    ) {
    }

    public record ListTenantsResponse(
            List<TenantItem> items
    ) {
    }
}

