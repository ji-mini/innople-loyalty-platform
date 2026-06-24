package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUserStatus;
import com.innople.loyalty.service.admin.AdminUserManagementService;
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

    public record UpdateStatusRequest(
            @NotNull AdminUserStatus status
    ) {
    }

    public record AdminUserResponse(
            UUID id,
            UUID tenantId,
            String phoneNumber,
            String email,
            String name,
            AdminRole role,
            AdminUserStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static AdminUserResponse from(AdminUserManagementService.AdminUserItem i) {
            return new AdminUserResponse(
                    i.id(),
                    i.tenantId(),
                    i.phoneNumber(),
                    i.email(),
                    i.name(),
                    i.role(),
                    i.status(),
                    i.createdAt(),
                    i.updatedAt()
            );
        }
    }
}

