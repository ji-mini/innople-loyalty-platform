package com.innople.loyalty.service.admin;

import com.innople.loyalty.domain.user.AdminRole;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AdminUserManagementService {

    List<AdminUserItem> list(String keyword);

    AdminUserItem create(String phoneNumber, String email, String name, String password, AdminRole role);

    AdminUserItem update(UUID adminUserId, String phoneNumber, String email, String name, AdminRole role);

    record AdminUserItem(
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

