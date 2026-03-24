package com.innople.loyalty.domain.points;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "point_lots",
        indexes = {
                @Index(name = "idx_point_lots_tenant_account", columnList = "tenantId,accountId"),
                @Index(name = "idx_point_lots_tenant_account_exp", columnList = "tenantId,accountId,expiresAt,createdAt"),
                @Index(name = "idx_point_lots_tenant_source_ledger", columnList = "tenantId,sourceLedgerId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLot extends BaseEntity {

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private long earnedAmount;

    @Column(nullable = false)
    private long remainingAmount;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = true)
    private UUID sourceLedgerId;

    public PointLot(UUID accountId, UUID memberId, long earnedAmount, Instant expiresAt, UUID sourceLedgerId) {
        this.accountId = accountId;
        this.memberId = memberId;
        this.earnedAmount = earnedAmount;
        this.remainingAmount = earnedAmount;
        this.expiresAt = expiresAt;
        this.sourceLedgerId = sourceLedgerId;
    }

    public void deduct(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (remainingAmount < amount) {
            throw new IllegalArgumentException("insufficient remainingAmount");
        }
        remainingAmount -= amount;
    }
}

