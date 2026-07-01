package com.innople.loyalty.repository;

import com.innople.loyalty.domain.user.AdminUserStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdminUserStatusHistoryRepository extends JpaRepository<AdminUserStatusHistory, UUID> {

    List<AdminUserStatusHistory> findByTenantIdAndAdminUserIdOrderByChangedAtDesc(UUID tenantId, UUID adminUserId);
}
