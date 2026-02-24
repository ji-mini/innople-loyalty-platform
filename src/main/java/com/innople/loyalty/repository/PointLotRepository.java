package com.innople.loyalty.repository;

import com.innople.loyalty.domain.points.PointLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointLotRepository extends JpaRepository<PointLot, UUID> {
    Optional<PointLot> findByTenantIdAndId(UUID tenantId, UUID id);

    @Query("""
            select l
            from PointLot l
            where l.tenantId = :tenantId
              and l.accountId = :accountId
              and l.remainingAmount > 0
              and l.expiresAt > :now
            order by l.expiresAt asc, l.createdAt asc
            """)
    List<PointLot> findDeductionCandidatesFefo(
            @Param("tenantId") UUID tenantId,
            @Param("accountId") UUID accountId,
            @Param("now") Instant now
    );

    @Query("""
            select l
            from PointLot l
            where l.tenantId = :tenantId
              and l.accountId = :accountId
              and l.remainingAmount > 0
              and l.expiresAt <= :referenceAt
            order by l.expiresAt asc, l.createdAt asc
            """)
    List<PointLot> findExpirableLotsFefo(
            @Param("tenantId") UUID tenantId,
            @Param("accountId") UUID accountId,
            @Param("referenceAt") Instant referenceAt
    );
}

