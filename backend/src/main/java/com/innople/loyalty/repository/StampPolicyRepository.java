package com.innople.loyalty.repository;

import com.innople.loyalty.domain.stamp.StampPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StampPolicyRepository extends JpaRepository<StampPolicy, UUID> {

    Optional<StampPolicy> findByTenantIdAndId(UUID tenantId, UUID id);

    List<StampPolicy> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    List<StampPolicy> findByTenantId(UUID tenantId);

    Optional<StampPolicy> findByTenantIdAndActiveTrue(UUID tenantId);
}
