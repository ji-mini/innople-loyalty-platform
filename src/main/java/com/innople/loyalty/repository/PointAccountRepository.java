package com.innople.loyalty.repository;

import com.innople.loyalty.domain.points.PointAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;
import java.util.UUID;

public interface PointAccountRepository extends JpaRepository<PointAccount, UUID> {
    Optional<PointAccount> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<PointAccount> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<PointAccount> findWithLockByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}

