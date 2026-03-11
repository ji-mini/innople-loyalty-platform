package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.user.AdminRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class AdminUserManagementDtos {
    private AdminUserManagementDtos() {
    }

    public record CreateRequest(
            @NotBlank String phoneNumber,
            String email,
            @NotBlank String name,
            @NotBlank String password,
            @NotNull AdminRole role
    ) {
    }

    public record UpdateRequest(
            @NotBlank String phoneNumber,
            String email,
            @NotBlank String name,
            @NotNull AdminRole role
    ) {
    }

    public record AdminUserResponse(
            UUID id,
            String phoneNumber,
            String email,
            String name,
            AdminRole role,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

