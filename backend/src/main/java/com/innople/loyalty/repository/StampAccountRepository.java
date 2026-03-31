package com.innople.loyalty.repository;

import com.innople.loyalty.domain.stamp.StampAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StampAccountRepository extends JpaRepository<StampAccount, UUID> {

    Optional<StampAccount> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    @Lock(LockModeType.OPTIMISTIC)
    Optional<StampAccount> findWithLockByTenantIdAndMemberId(UUID tenantId, UUID memberId);

    @Query("""
            select a from StampAccount a
            where a.tenantId = :tenantId
              and a.currentBalance >= :minBalance
            """)
    List<StampAccount> findByTenantIdAndCurrentBalanceAtLeast(
            @Param("tenantId") UUID tenantId,
            @Param("minBalance") int minBalance
    );
}
