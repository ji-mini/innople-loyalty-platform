package com.innople.loyalty.repository;

import com.innople.loyalty.domain.points.PointPolicy;
import com.innople.loyalty.domain.points.PointPolicyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, UUID> {
    List<PointPolicy> findAllByTenantIdOrderByUpdatedAtDesc(UUID tenantId);

    Optional<PointPolicy> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndPointType(UUID tenantId, PointPolicyType pointType);
}

