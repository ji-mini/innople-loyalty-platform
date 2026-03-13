package com.innople.loyalty.repository;

import com.innople.loyalty.domain.log.ApiAuditCategory;
import com.innople.loyalty.domain.log.ApiAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ApiAuditLogRepository extends JpaRepository<ApiAuditLog, UUID>, JpaSpecificationExecutor<ApiAuditLog> {

    List<ApiAuditLog> findTop20ByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}

