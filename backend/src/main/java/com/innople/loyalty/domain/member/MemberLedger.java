package com.innople.loyalty.domain.member;

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
        name = "member_ledgers",
        indexes = {
                @Index(name = "idx_member_ledgers_tenant_id", columnList = "tenantId"),
                @Index(name = "idx_member_ledgers_tenant_member_id", columnList = "tenantId,memberId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberLedger extends BaseEntity {

    /**
     * Canonical member reference for relational integrity.
     */
    @Column(nullable = false)
    private UUID memberId;

    /**
     * Snapshot/display value kept for audit readability.
     */
    @Column(nullable = false, length = 50)
    private String memberNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberLedgerEventType eventType;

    @Column(nullable = false, length = 50)
    private String statusCodeBefore;

    @Column(nullable = false, length = 50)
    private String statusCodeAfter;

    @Column(nullable = true, length = 2000)
    private String snapshotJson;

    public static MemberLedger of(
            UUID memberId,
            String memberNo,
            MemberLedgerEventType eventType,
            String statusCodeBefore,
            String statusCodeAfter,
            String snapshotJson
    ) {
        MemberLedger ledger = new MemberLedger();
        ledger.memberId = memberId;
        ledger.memberNo = memberNo;
        ledger.eventType = eventType;
        ledger.statusCodeBefore = statusCodeBefore;
        ledger.statusCodeAfter = statusCodeAfter;
        ledger.snapshotJson = snapshotJson;
        return ledger;
    }
}

