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
                @Index(name = "idx_point_ledgers_tenant_member", columnList = "tenantId,memberId"),
                @Index(name = "uk_point_ledgers_tenant_approval", columnList = "tenantId,approvalNo", unique = true),
                @Index(name = "idx_point_ledgers_tenant_reference", columnList = "tenantId,referenceType,referenceId")
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

    @Column(nullable = false, length = 40)
    private String sourceChannel;

    @Column(nullable = false, length = 12)
    private String approvalNo;

    @Column(length = 50)
    private String referenceType;

    @Column(length = 100)
    private String referenceId;

    /** 적립 시 기준 적립 대상 금액(원). POS 연동 시 설정, 수기 적립은 null. */
    @Column(nullable = true)
    private Long purchaseAmount;

    /** 총 구매금액(원). POS 연동 시 선택. */
    @Column(nullable = true)
    private Long totalPurchaseAmount;

    /** 할인금액(원). POS 연동 시 선택. */
    @Column(nullable = true)
    private Long discountAmount;

    public PointLedger(UUID accountId, UUID memberId, PointEventType eventType, long amount, String reason,
                       String sourceChannel, String approvalNo, String referenceType, String referenceId) {
        this(accountId, memberId, eventType, amount, reason, sourceChannel, approvalNo, referenceType, referenceId,
                null, null, null);
    }

    public PointLedger(UUID accountId, UUID memberId, PointEventType eventType, long amount, String reason,
                       String sourceChannel, String approvalNo, String referenceType, String referenceId,
                       Long purchaseAmount, Long totalPurchaseAmount, Long discountAmount) {
        this.accountId = accountId;
        this.memberId = memberId;
        this.eventType = eventType;
        this.amount = amount;
        this.reason = reason;
        this.sourceChannel = sourceChannel;
        this.approvalNo = approvalNo;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.purchaseAmount = purchaseAmount;
        this.totalPurchaseAmount = totalPurchaseAmount;
        this.discountAmount = discountAmount;
    }
}

