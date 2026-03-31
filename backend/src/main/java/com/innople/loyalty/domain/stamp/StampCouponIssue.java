package com.innople.loyalty.domain.stamp;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "stamp_coupon_issues",
        indexes = {
                @Index(name = "idx_stamp_coupon_issues_tenant_member", columnList = "tenantId,memberId"),
                @Index(
                        name = "uk_stamp_coupon_issues_tenant_redemption_ledger",
                        columnList = "tenantId,redemptionLedgerId",
                        unique = true
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampCouponIssue extends BaseEntity {

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false, name = "stamp_policy_id")
    private UUID stampPolicyId;

    @Column(nullable = false, name = "coupon_template_id")
    private UUID couponTemplateId;

    @Column(nullable = false, name = "redemption_ledger_id")
    private UUID redemptionLedgerId;

    public StampCouponIssue(
            UUID memberId,
            UUID stampPolicyId,
            UUID couponTemplateId,
            UUID redemptionLedgerId
    ) {
        this.memberId = memberId;
        this.stampPolicyId = stampPolicyId;
        this.couponTemplateId = couponTemplateId;
        this.redemptionLedgerId = redemptionLedgerId;
    }
}
