package com.innople.loyalty.repository;

import com.innople.loyalty.domain.coupon.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, UUID> {

    Optional<CouponTemplate> findByTenantIdAndId(UUID tenantId, UUID id);

    List<CouponTemplate> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    boolean existsByTenantIdAndId(UUID tenantId, UUID id);
}
