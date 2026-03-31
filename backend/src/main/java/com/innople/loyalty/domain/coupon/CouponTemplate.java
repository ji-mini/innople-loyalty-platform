package com.innople.loyalty.domain.coupon;

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
        name = "coupon_templates",
        indexes = @Index(name = "idx_coupon_templates_tenant_id", columnList = "tenantId")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponTemplate extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public CouponTemplate(String name, String description, boolean active) {
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public void update(String name, String description, boolean active) {
        this.name = name;
        this.description = description;
        this.active = active;
    }
}
