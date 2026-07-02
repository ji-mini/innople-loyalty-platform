package com.innople.loyalty.repository;

import com.innople.loyalty.domain.user.AdminUserRoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminUserRoleHistoryRepository extends JpaRepository<AdminUserRoleHistory, UUID> {

    List<AdminUserRoleHistory> findByTenantIdAndAdminUserIdOrderByChangedAtDesc(UUID tenantId, UUID adminUserId);
}
