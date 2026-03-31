package com.innople.loyalty.repository;

import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.domain.stamp.StampLedger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StampLedgerRepository extends JpaRepository<StampLedger, UUID> {

    boolean existsByTenantIdAndReferenceTypeAndReferenceId(UUID tenantId, String referenceType, String referenceId);

    Optional<StampLedger> findByTenantIdAndId(UUID tenantId, UUID id);

    @Query("""
            select new com.innople.loyalty.controller.dto.StampDtos$StampLedgerRow(
                l.id,
                m.memberNo,
                l.eventType,
                l.stampDelta,
                l.reason,
                l.referenceType,
                l.referenceId,
                l.purchaseAmountWon,
                l.createdAt
            )
            from StampLedger l, com.innople.loyalty.domain.member.Member m
            where l.memberId = m.id and l.tenantId = m.tenantId
              and l.tenantId = :tenantId
              and (:memberNo is null or m.memberNo = :memberNo)
            order by l.createdAt desc
            """)
    List<StampDtos.StampLedgerRow> findLedgersForTenant(
            @Param("tenantId") UUID tenantId,
            @Param("memberNo") String memberNo,
            Pageable pageable
    );
}
