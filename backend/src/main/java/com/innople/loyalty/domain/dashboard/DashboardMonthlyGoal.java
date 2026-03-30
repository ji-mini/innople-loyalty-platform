package com.innople.loyalty.domain.dashboard;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "dashboard_monthly_goal",
        indexes = {
                @Index(name = "uk_dashboard_monthly_goal_tenant_year_month", columnList = "tenantId,targetYear,targetMonth", unique = true)
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardMonthlyGoal extends BaseEntity {

    @Column(nullable = false)
    private int targetYear;

    @Column(nullable = false)
    private int targetMonth;

    @Column(nullable = false)
    private long targetNewMembers;

    @Column(nullable = false)
    private long targetEarn;

    @Column(nullable = false)
    private long targetUse;

    public DashboardMonthlyGoal(int targetYear, int targetMonth, long targetNewMembers, long targetEarn, long targetUse) {
        validateYearMonth(targetYear, targetMonth);
        validateNonNegative(targetNewMembers, "targetNewMembers");
        validateNonNegative(targetEarn, "targetEarn");
        validateNonNegative(targetUse, "targetUse");
        this.targetYear = targetYear;
        this.targetMonth = targetMonth;
        this.targetNewMembers = targetNewMembers;
        this.targetEarn = targetEarn;
        this.targetUse = targetUse;
    }

    public void changeTargets(long targetNewMembers, long targetEarn, long targetUse) {
        validateNonNegative(targetNewMembers, "targetNewMembers");
        validateNonNegative(targetEarn, "targetEarn");
        validateNonNegative(targetUse, "targetUse");
        this.targetNewMembers = targetNewMembers;
        this.targetEarn = targetEarn;
        this.targetUse = targetUse;
    }

    private static void validateYearMonth(int targetYear, int targetMonth) {
        if (targetYear < 2000 || targetYear > 9999) {
            throw new IllegalArgumentException("targetYear is invalid");
        }
        if (targetMonth < 1 || targetMonth > 12) {
            throw new IllegalArgumentException("targetMonth is invalid");
        }
    }

    private static void validateNonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative");
        }
    }
}
