package com.innople.loyalty.service.tenant;

import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantQueryServiceImpl implements TenantQueryService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TenantListItem> listTenants() {
        List<Tenant> tenants = tenantRepository.findAll(Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
        return tenants.stream()
                .map(t -> new TenantListItem(t.getTenantId(), t.getName()))
                .toList();
    }
}

