package com.innople.loyalty.repository;

import com.innople.loyalty.domain.batch.BatchJobConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchJobConfigRepository extends JpaRepository<BatchJobConfig, UUID> {

    Optional<BatchJobConfig> findByTenantIdAndBatchName(UUID tenantId, String batchName);

    List<BatchJobConfig> findByTenantIdOrderByBatchNameAsc(UUID tenantId);

    boolean existsByTenantIdAndBatchName(UUID tenantId, String batchName);
}
