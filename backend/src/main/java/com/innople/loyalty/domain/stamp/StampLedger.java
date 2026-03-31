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
        name = "stamp_ledgers",
        indexes = {
                @Index(name = "idx_stamp_ledgers_tenant_member", columnList = "tenantId,memberId"),
                @Index(name = "idx_stamp_ledgers_tenant_created", columnList = "tenantId,createdAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampLedger extends BaseEntity {

    @Column(nullable = false, name = "account_id")
    private UUID accountId;

    @Column(nullable = false)
    private UUID memberId;

    @Column(name = "policy_id")
    private UUID policyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StampEventType eventType;

    @Column(nullable = false, name = "stamp_delta")
    private int stampDelta;

    @Column(length = 500)
    private String reason;

    @Column(length = 50)
    private String referenceType;

    @Column(length = 100)
    private String referenceId;

    @Column(name = "purchase_amount_won")
    private Long purchaseAmountWon;

    public StampLedger(
            UUID accountId,
            UUID memberId,
            UUID policyId,
            StampEventType eventType,
            int stampDelta,
            String reason,
            String referenceType,
            String referenceId,
            Long purchaseAmountWon
    ) {
        this.accountId = accountId;
        this.memberId = memberId;
        this.policyId = policyId;
        this.eventType = eventType;
        this.stampDelta = stampDelta;
        this.reason = reason;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.purchaseAmountWon = purchaseAmountWon;
    }
}
