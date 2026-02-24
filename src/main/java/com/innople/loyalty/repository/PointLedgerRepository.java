package com.innople.loyalty.repository;

import com.innople.loyalty.domain.points.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointLedgerRepository extends JpaRepository<PointLedger, UUID> {
    Optional<PointLedger> findByTenantIdAndId(UUID tenantId, UUID id);

    List<PointLedger> findTop50ByTenantIdAndAccountIdOrderByCreatedAtDesc(UUID tenantId, UUID accountId);
}

