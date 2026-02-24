package com.innople.loyalty.repository;

import com.innople.loyalty.domain.user.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    Optional<AdminUser> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<AdminUser> findByTenantIdAndEmail(UUID tenantId, String email);
}

