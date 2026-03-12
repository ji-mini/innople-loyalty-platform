package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.TenantAdminDtos;
import com.innople.loyalty.service.tenant.TenantAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tenants")
@RequiredArgsConstructor
public class TenantAdminController {

    private final TenantAdminService tenantAdminService;

    @GetMapping("/{tenantId}")
    public TenantAdminDtos.TenantResponse get(@PathVariable UUID tenantId) {
        return toResponse(tenantAdminService.get(tenantId));
    }

    @PutMapping("/{tenantId}")
    public TenantAdminDtos.TenantResponse update(
            @PathVariable UUID tenantId,
            @Valid @RequestBody TenantAdminDtos.UpdateRequest request
    ) {
        return toResponse(tenantAdminService.update(tenantId, request.name()));
    }

    private TenantAdminDtos.TenantResponse toResponse(TenantAdminService.TenantDetail d) {
        return new TenantAdminDtos.TenantResponse(d.tenantId(), d.name(), d.createdAt(), d.updatedAt());
    }
}

