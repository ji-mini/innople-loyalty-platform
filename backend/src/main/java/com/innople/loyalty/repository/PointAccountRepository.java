package com.innople.loyalty.repository;

import com.innople.loyalty.domain.points.PointAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointAccountRepository extends JpaRepository<PointAccount, UUID> {

    @Query("select coalesce(sum(pa.currentBalance), 0) from PointAccount pa where pa.tenantId = :tenantId")
    long sumCurrentBalanceByTenantId(@Param("tenantId") UUID tenantId);
    Optional<PointAccount> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<PointAccount> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    List<PointAccount> findAllByTenantId(UUID tenantId);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<PointAccount> findWithLockByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}

