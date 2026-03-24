package com.innople.loyalty.domain.points;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointPolicy extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String pointType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int validityDays;

    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 500)
    private String description;

    public PointPolicy(String pointType, String name, int validityDays, boolean enabled, String description) {
        String normalizedPointType = normalizeRequired(pointType, "pointType");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (validityDays <= 0) {
            throw new IllegalArgumentException("validityDays must be positive");
        }
        this.pointType = normalizedPointType;
        this.name = name.trim();
        this.validityDays = validityDays;
        this.enabled = enabled;
        this.description = (description == null || description.isBlank()) ? null : description.trim();
    }

    public void change(String pointType, String name, int validityDays, boolean enabled, String description) {
        String normalizedPointType = normalizeRequired(pointType, "pointType");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (validityDays <= 0) {
            throw new IllegalArgumentException("validityDays must be positive");
        }
        this.pointType = normalizedPointType;
        this.name = name.trim();
        this.validityDays = validityDays;
        this.enabled = enabled;
        this.description = (description == null || description.isBlank()) ? null : description.trim();
    }

    private static String normalizeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}

