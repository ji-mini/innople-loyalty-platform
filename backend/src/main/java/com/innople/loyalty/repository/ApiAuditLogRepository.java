package com.innople.loyalty.repository;

import com.innople.loyalty.domain.log.ApiAuditCategory;
import com.innople.loyalty.domain.log.ApiAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ApiAuditLogRepository extends JpaRepository<ApiAuditLog, UUID> {

    @Query("""
            select l
            from ApiAuditLog l
            where l.tenantId = :tenantId
              and (:category is null or l.category = :category)
              and (:fromAt is null or l.createdAt >= :fromAt)
              and (:toAt is null or l.createdAt <= :toAt)
              and (
                :keyword is null
                or :keyword = ''
                or lower(l.path) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(l.message, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(l.userAgent, '')) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<ApiAuditLog> search(
            @Param("tenantId") UUID tenantId,
            @Param("category") ApiAuditCategory category,
            @Param("fromAt") Instant fromAt,
            @Param("toAt") Instant toAt,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

