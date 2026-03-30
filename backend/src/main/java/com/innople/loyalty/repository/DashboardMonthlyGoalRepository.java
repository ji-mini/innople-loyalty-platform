package com.innople.loyalty.repository;

import com.innople.loyalty.domain.dashboard.DashboardMonthlyGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DashboardMonthlyGoalRepository extends JpaRepository<DashboardMonthlyGoal, UUID> {
    Optional<DashboardMonthlyGoal> findByTenantIdAndTargetYearAndTargetMonth(UUID tenantId, int targetYear, int targetMonth);
}
