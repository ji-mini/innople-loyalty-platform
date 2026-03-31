package com.innople.loyalty.domain.stamp;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "stamp_policies",
        indexes = @Index(name = "idx_stamp_policies_tenant_id", columnList = "tenantId")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampPolicy extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    /** N원당 스탬프 1개 — 구매 금액을 이 값으로 나눈 몫만큼 적립 */
    @Column(nullable = false, name = "amount_won_per_stamp")
    private long amountWonPerStamp;

    @Column(nullable = false, name = "stamps_required_for_coupon")
    private int stampsRequiredForCoupon;

    @Column(nullable = false, name = "coupon_template_id")
    private UUID couponTemplateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StampIssuanceMode issuanceMode;

    @Column(nullable = false)
    private boolean active = true;

    public StampPolicy(
            String name,
            long amountWonPerStamp,
            int stampsRequiredForCoupon,
            UUID couponTemplateId,
            StampIssuanceMode issuanceMode,
            boolean active
    ) {
        this.name = name;
        this.amountWonPerStamp = amountWonPerStamp;
        this.stampsRequiredForCoupon = stampsRequiredForCoupon;
        this.couponTemplateId = couponTemplateId;
        this.issuanceMode = issuanceMode;
        this.active = active;
    }

    public void update(
            String name,
            long amountWonPerStamp,
            int stampsRequiredForCoupon,
            UUID couponTemplateId,
            StampIssuanceMode issuanceMode,
            boolean active
    ) {
        this.name = name;
        this.amountWonPerStamp = amountWonPerStamp;
        this.stampsRequiredForCoupon = stampsRequiredForCoupon;
        this.couponTemplateId = couponTemplateId;
        this.issuanceMode = issuanceMode;
        this.active = active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
