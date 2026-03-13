package com.innople.loyalty.service.tenant;

import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantQueryServiceImpl implements TenantQueryService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TenantListItem> listTenants() {
        List<Tenant> tenants = tenantRepository.findAll(Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
        return tenants.stream()
                .map(t -> new TenantListItem(t.getTenantId(), t.getName(), t.getRepresentativeCode()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TenantListItem> findByTenantId(UUID tenantId) {
        return tenantRepository.findByTenantId(tenantId)
                .map(t -> new TenantListItem(t.getTenantId(), t.getName(), t.getRepresentativeCode()));
    }
}

