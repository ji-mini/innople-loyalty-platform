package com.innople.loyalty.service.tenant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantQueryService {
    List<TenantListItem> listTenants();

    Optional<TenantListItem> findByTenantId(UUID tenantId);
}

