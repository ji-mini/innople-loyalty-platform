package com.innople.loyalty.service.admin;

import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AdminUserManagementService {

    List<AdminUserItem> list(String keyword);

    AdminUserItem create(String phoneNumber, String email, String name, String password, AdminRole role);

    AdminUserItem update(UUID adminUserId, String phoneNumber, String email, String name, AdminRole role);

    AdminUserItem updateStatus(UUID adminUserId, AdminUserStatus status);

    record AdminUserItem(
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
    }
}

