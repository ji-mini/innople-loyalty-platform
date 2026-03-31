package com.innople.loyalty.repository;

import com.innople.loyalty.domain.stamp.StampCouponIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StampCouponIssueRepository extends JpaRepository<StampCouponIssue, UUID> {

    boolean existsByTenantIdAndRedemptionLedgerId(UUID tenantId, UUID redemptionLedgerId);
}
