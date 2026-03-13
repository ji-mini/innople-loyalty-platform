package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public final class TenantAdminDtos {
    private TenantAdminDtos() {
    }

    public record TenantResponse(
            UUID tenantId,
            String name,
            String representativeCode,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record UpdateRequest(
            @NotBlank String name
    ) {
    }
}

