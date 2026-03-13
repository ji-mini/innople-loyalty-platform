package com.innople.loyalty.domain.member;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public MembershipGrade(String name, int level, String description) {
        this.name = name;
        this.level = level;
        this.description = description;
    }

    public void update(String name, int level, String description) {
        this.name = name;
        this.level = level;
        this.description = description;
    }
}

