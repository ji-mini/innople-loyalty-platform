package com.innople.loyalty.repository;

import com.innople.loyalty.domain.batch.BatchExecutionHistory;
import com.innople.loyalty.domain.batch.BatchExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BatchExecutionHistoryRepository extends JpaRepository<BatchExecutionHistory, UUID> {

    Page<BatchExecutionHistory> findByTenantId(UUID tenantId, Pageable pageable);

    Page<BatchExecutionHistory> findByTenantIdAndBatchName(UUID tenantId, String batchName, Pageable pageable);

    /**
     * catch-up 판정: 특정 테넌트+배치가 [from, toExclusive) (오늘 KST 범위) 안에서
     * 이미 완료(status ∈ statuses, 통상 SUCCESS/PARTIAL)된 이력이 있는지.
     */
    @Query("""
            select count(h) > 0
            from BatchExecutionHistory h
            where h.tenantId = :tenantId
              and h.batchName = :batchName
              and h.status in :statuses
              and h.startedAt >= :from
              and h.startedAt < :toExclusive
            """)
    boolean existsCompletedInRange(
            @Param("tenantId") UUID tenantId,
            @Param("batchName") String batchName,
            @Param("statuses") Collection<BatchExecutionStatus> statuses,
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive
    );

    /**
     * 현재 테넌트의 batch_name별 최신 실행 시각(MAX(started_at))을 집계 1회로 조회한다.
     * config 목록에 마지막 실행일시를 매핑할 때 N+1을 피하기 위해 사용한다.
     */
    @Query("""
            select h.batchName as batchName, max(h.startedAt) as lastStartedAt
            from BatchExecutionHistory h
            where h.tenantId = :tenantId
            group by h.batchName
            """)
    List<BatchNameLastStartedAt> findLastStartedAtByTenantGrouped(@Param("tenantId") UUID tenantId);

    /**
     * 특정 (tenant, batchName)의 최신 실행 시각. 이력이 없으면 null.
     */
    @Query("""
            select max(h.startedAt)
            from BatchExecutionHistory h
            where h.tenantId = :tenantId
              and h.batchName = :batchName
            """)
    Instant findLastStartedAt(@Param("tenantId") UUID tenantId, @Param("batchName") String batchName);

    /** batch_name → 최신 started_at 집계 투영. */
    interface BatchNameLastStartedAt {
        String getBatchName();

        Instant getLastStartedAt();
    }
}
