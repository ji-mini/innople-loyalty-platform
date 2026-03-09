package com.innople.loyalty.config;

import java.util.Optional;
import java.util.UUID;

public final class TenantContext {
    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Optional<UUID> getTenantId() {
        return Optional.ofNullable(TENANT_ID.get());
    }

    public static UUID requireTenantId() {
        return getTenantId().orElseThrow(() -> new TenantMissingException("tenantId is missing"));
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}

