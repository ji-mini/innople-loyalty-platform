package com.innople.loyalty.domain.member;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "membership_grades",
        indexes = {
                @Index(name = "idx_membership_grades_tenant_id", columnList = "tenantId"),
                @Index(name = "idx_membership_grades_tenant_level", columnList = "tenantId,level", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MembershipGrade extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private int level;

    @Column(nullable = true, length = 500)
    private String description;

    /**
     * 적립 대상 금액 대비 적립률(%). POS 등에서 적립 대상 금액을 받아 등급별로 포인트를 계산할 때 사용합니다.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal earnRatePercent;

    public MembershipGrade(String name, int level, String description, BigDecimal earnRatePercent) {
        this.name = name;
        this.level = level;
        this.description = description;
        this.earnRatePercent = earnRatePercent != null ? earnRatePercent : BigDecimal.ZERO;
    }

    public void update(String name, int level, String description, BigDecimal earnRatePercent) {
        this.name = name;
        this.level = level;
        this.description = description;
        this.earnRatePercent = earnRatePercent != null ? earnRatePercent : BigDecimal.ZERO;
    }
}

