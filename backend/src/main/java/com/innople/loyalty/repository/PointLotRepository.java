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

    Optional<PointLot> findByTenantIdAndSourceLedgerId(UUID tenantId, UUID sourceLedgerId);

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

    // 탈회 소각용: 만료일 도래 여부와 무관하게 잔량이 남은 모든 lot 을 조회한다.
    // (만료일이 이미 지났지만 만료 배치가 아직 처리하지 않은 lot 도 포함) FEFO 정렬은 소각 순서 안정성을 위해 유지한다.
    @Query("""
            select l
            from PointLot l
            where l.tenantId = :tenantId
              and l.accountId = :accountId
              and l.remainingAmount > 0
            order by l.expiresAt asc, l.createdAt asc
            """)
    List<PointLot> findAllRemainingLots(
            @Param("tenantId") UUID tenantId,
            @Param("accountId") UUID accountId
    );

    @Query("""
            select distinct l.memberId
            from PointLot l
            where l.tenantId = :tenantId
              and l.remainingAmount > 0
              and l.expiresAt <= :referenceAt
            """)
    List<UUID> findDistinctMemberIdsWithExpirableLots(
            @Param("tenantId") UUID tenantId,
            @Param("referenceAt") Instant referenceAt
    );

    /** 미사용 포인트 총 잔액. lot 잔여분 합계. */
    @Query("""
            select coalesce(sum(l.remainingAmount), 0)
            from PointLot l
            where l.tenantId = :tenantId
              and l.remainingAmount > 0
            """)
    long sumRemainingAmountByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * 기준 시각 이전에 만료되는 잔여분 합계(소멸 예정 금액).
     * deadline 은 DateRangeUtils 로 산출한 KST half-open 구간의 종료 경계를 넘긴다.
     */
    @Query("""
            select coalesce(sum(l.remainingAmount), 0)
            from PointLot l
            where l.tenantId = :tenantId
              and l.remainingAmount > 0
              and l.expiresAt < :deadline
            """)
    long sumRemainingAmountExpiringBefore(
            @Param("tenantId") UUID tenantId,
            @Param("deadline") Instant deadline
    );
}

