package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.TenantPublicDtos;
import com.innople.loyalty.service.tenant.TenantListItem;
import com.innople.loyalty.service.tenant.TenantQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/tenants")
@RequiredArgsConstructor
public class PublicTenantController {

    private final TenantQueryService tenantQueryService;

    @GetMapping
    public TenantPublicDtos.ListTenantsResponse listTenants() {
        List<TenantListItem> items = tenantQueryService.listTenants();
        return new TenantPublicDtos.ListTenantsResponse(
                items.stream()
                        .map(i -> new TenantPublicDtos.TenantItem(i.tenantId(), i.name(), i.representativeCode()))
                        .toList()
        );
    }
}

