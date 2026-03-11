package com.innople.loyalty.domain.points;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointPolicyType pointType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int validityDays;

    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 500)
    private String description;

    public PointPolicy(PointPolicyType pointType, String name, int validityDays, boolean enabled, String description) {
        if (pointType == null) {
            throw new IllegalArgumentException("pointType must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (validityDays <= 0) {
            throw new IllegalArgumentException("validityDays must be positive");
        }
        this.pointType = pointType;
        this.name = name.trim();
        this.validityDays = validityDays;
        this.enabled = enabled;
        this.description = (description == null || description.isBlank()) ? null : description.trim();
    }

    public void change(PointPolicyType pointType, String name, int validityDays, boolean enabled, String description) {
        if (pointType == null) {
            throw new IllegalArgumentException("pointType must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (validityDays <= 0) {
            throw new IllegalArgumentException("validityDays must be positive");
        }
        this.pointType = pointType;
        this.name = name.trim();
        this.validityDays = validityDays;
        this.enabled = enabled;
        this.description = (description == null || description.isBlank()) ? null : description.trim();
    }
}

