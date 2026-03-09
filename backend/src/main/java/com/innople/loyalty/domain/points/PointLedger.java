package com.innople.loyalty.domain.points;

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
        name = "point_ledgers",
        indexes = {
                @Index(name = "idx_point_ledgers_tenant_account", columnList = "tenantId,accountId"),
                @Index(name = "idx_point_ledgers_tenant_member", columnList = "tenantId,memberId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLedger extends BaseEntity {

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointEventType eventType;

    /**
     * Signed amount.
     * EARN/ADJUST_EARN: +amount
     * USE/EXPIRE/ADJUST_USE: -amount
     */
    @Column(nullable = false)
    private long amount;

    @Column(nullable = true, length = 500)
    private String reason;

    public PointLedger(UUID accountId, UUID memberId, PointEventType eventType, long amount, String reason) {
        this.accountId = accountId;
        this.memberId = memberId;
        this.eventType = eventType;
        this.amount = amount;
        this.reason = reason;
    }
}

