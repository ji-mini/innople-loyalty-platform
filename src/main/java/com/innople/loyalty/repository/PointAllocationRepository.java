package com.innople.loyalty.repository;

import com.innople.loyalty.domain.points.PointAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointAllocationRepository extends JpaRepository<PointAllocation, UUID> {
    Optional<PointAllocation> findByTenantIdAndId(UUID tenantId, UUID id);

    List<PointAllocation> findByTenantIdAndLedgerId(UUID tenantId, UUID ledgerId);
}

