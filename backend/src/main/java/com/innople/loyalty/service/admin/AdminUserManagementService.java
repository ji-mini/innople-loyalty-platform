package com.innople.loyalty.service.admin;

import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AdminUserManagementService {

    List<AdminUserItem> list(String keyword);

    AdminUserItem create(String phoneNumber, String email, String name, String password, AdminRole role);

    AdminUserItem update(UUID adminUserId, String phoneNumber, String email, String name, AdminRole role, UUID changedBy);

    /**
     * 계정 상태를 변경한다. targetRole이 주어지고 현재 role과 다르면 승인과 동시에 권한도 변경한다.
     * (예: 사용자 → 관리자로 승격하며 승인) role 변경이 실제로 발생한 경우에만 권한 변경 이력을 남긴다.
     */
    AdminUserItem updateStatus(UUID adminUserId, AdminUserStatus status, AdminRole targetRole, UUID changedBy, String reason);

    /**
     * 승인과 무관하게 이미 ACTIVE인 계정의 권한(role)만 단독으로 변경한다.
     * role이 실제로 변경된 경우에만 이력을 남긴다.
     */
    AdminUserItem updateRole(UUID adminUserId, AdminRole role, UUID changedBy, String reason);

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

