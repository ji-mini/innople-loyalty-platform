package com.innople.loyalty.repository;

import com.innople.loyalty.controller.dto.PointDtos;
import com.innople.loyalty.domain.points.PointEventType;
import com.innople.loyalty.domain.points.PointLedger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointLedgerRepository extends JpaRepository<PointLedger, UUID> {
    Optional<PointLedger> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndApprovalNo(UUID tenantId, String approvalNo);

    List<PointLedger> findTop50ByTenantIdAndAccountIdOrderByCreatedAtDesc(UUID tenantId, UUID accountId);

    @Query("""
            select new com.innople.loyalty.controller.dto.PointDtos$PointLedgerResponse(
                l.id,
                m.memberNo,
                l.eventType,
                l.amount,
                l.reason,
                l.sourceChannel,
                case
                    when l.eventType in ('EARN', 'ADJUST_EARN')
                        then (select pl.expiresAt
                              from PointLot pl
                              where pl.tenantId = l.tenantId and pl.sourceLedgerId = l.id)
                    else (select min(pl2.expiresAt)
                          from PointAllocation pa, PointLot pl2
                          where pa.tenantId = l.tenantId
                            and pa.ledgerId = l.id
                            and pa.lotId = pl2.id
                            and pa.tenantId = pl2.tenantId)
                end,
                l.approvalNo,
                l.referenceType,
                l.referenceId,
                l.createdAt
            ) from PointLedger l, com.innople.loyalty.domain.member.Member m
            where l.memberId = m.id and l.tenantId = m.tenantId
            and l.tenantId = :tenantId
            and (:memberNo is null or m.memberNo = :memberNo)
            order by l.createdAt desc
            """)
    List<PointDtos.PointLedgerResponse> findLedgersForTenant(
            @Param("tenantId") UUID tenantId,
            @Param("memberNo") String memberNo,
            Pageable pageable);

    @Query("""
            select coalesce(sum(l.amount), 0) from PointLedger l
            where l.tenantId = :tenantId and l.createdAt >= :from and l.createdAt < :to
            and l.eventType in ('EARN', 'ADJUST_EARN')
            """)
    long sumEarnByTenantIdAndCreatedAtBetween(
            @Param("tenantId") UUID tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select coalesce(sum(abs(l.amount)), 0) from PointLedger l
            where l.tenantId = :tenantId and l.createdAt >= :from and l.createdAt < :to
            and l.eventType in ('USE', 'EXPIRE_AUTO', 'EXPIRE_MANUAL', 'ADJUST_USE')
            """)
    long sumUseByTenantIdAndCreatedAtBetween(
            @Param("tenantId") UUID tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
            select coalesce(sum(l.amount), 0) from PointLedger l
            where l.tenantId = :tenantId
              and l.memberId = :memberId
            """)
    long sumBalanceByTenantIdAndMemberId(
            @Param("tenantId") UUID tenantId,
            @Param("memberId") UUID memberId
    );

    @Query("""
            select distinct l.memberId
            from PointLedger l
            where l.tenantId = :tenantId
            """)
    List<UUID> findDistinctMemberIdsByTenantId(@Param("tenantId") UUID tenantId);

    List<PointLedger> findTop20ByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}

