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

    boolean existsByTenantIdAndReferenceTypeAndReferenceId(UUID tenantId, String referenceType, String referenceId);

    Optional<PointLedger> findFirstByTenantIdAndReferenceTypeAndReferenceIdOrderByCreatedAtDesc(
            UUID tenantId,
            String referenceType,
            String referenceId
    );

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
                l.purchaseAmount,
                l.totalPurchaseAmount,
                l.discountAmount,
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

    /** 누적 총 적립액(기간 제한 없음). 대시보드 "누적 총 적립 포인트" 용. */
    @Query("""
            select coalesce(sum(l.amount), 0) from PointLedger l
            where l.tenantId = :tenantId
            and l.eventType in ('EARN', 'ADJUST_EARN')
            """)
    long sumTotalEarnByTenantId(@Param("tenantId") UUID tenantId);

    /** 누적 총 사용액(만료/소각 제외, 기간 제한 없음). 대시보드 "누적 총 사용 포인트" 용. */
    @Query("""
            select coalesce(sum(abs(l.amount)), 0) from PointLedger l
            where l.tenantId = :tenantId
            and l.eventType in ('USE', 'ADJUST_USE')
            """)
    long sumTotalPureUseByTenantId(@Param("tenantId") UUID tenantId);

    /** 순수 사용액(만료/소각 제외). 대시보드 "오늘 사용 포인트" 용. */
    @Query("""
            select coalesce(sum(abs(l.amount)), 0) from PointLedger l
            where l.tenantId = :tenantId and l.createdAt >= :from and l.createdAt < :to
            and l.eventType in ('USE', 'ADJUST_USE')
            """)
    long sumPureUseByTenantIdAndCreatedAtBetween(
            @Param("tenantId") UUID tenantId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    /**
     * KST 일자별 적립/사용 합계. 구간 경계는 DateRangeUtils 로 산출한 half-open Instant 를 넘긴다.
     * 반환: [bucketDate(String, yyyy-MM-dd), earned(Long), used(Long)]
     */
    @Query(value = """
            select to_char(l.created_at at time zone cast(:zone as text), 'YYYY-MM-DD') as bucket_date,
                   cast(coalesce(sum(case when l.event_type in ('EARN', 'ADJUST_EARN') then l.amount else 0 end), 0) as bigint) as earned,
                   cast(coalesce(sum(case when l.event_type in ('USE', 'ADJUST_USE') then abs(l.amount) else 0 end), 0) as bigint) as used
            from point_ledgers l
            where l.tenant_id = :tenantId
              and l.created_at >= :from
              and l.created_at < :toExclusive
            group by 1
            """, nativeQuery = true)
    List<Object[]> aggregateDailyEarnAndUse(
            @Param("tenantId") UUID tenantId,
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive,
            @Param("zone") String zone
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

