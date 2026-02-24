package com.innople.loyalty.domain.points;

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
        name = "point_allocations",
        indexes = {
                @Index(name = "idx_point_allocations_tenant_ledger", columnList = "tenantId,ledgerId"),
                @Index(name = "idx_point_allocations_tenant_lot", columnList = "tenantId,lotId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointAllocation extends BaseEntity {

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID ledgerId;

    @Column(nullable = false)
    private UUID lotId;

    @Column(nullable = false)
    private long allocatedAmount;

    public PointAllocation(UUID accountId, UUID ledgerId, UUID lotId, long allocatedAmount) {
        this.accountId = accountId;
        this.ledgerId = ledgerId;
        this.lotId = lotId;
        this.allocatedAmount = allocatedAmount;
    }
}

