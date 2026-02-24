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

@Entity
@Table(
        name = "member_ledgers",
        indexes = {
                @Index(name = "idx_member_ledgers_tenant_id", columnList = "tenantId"),
                @Index(name = "idx_member_ledgers_tenant_member_no", columnList = "tenantId,memberNo")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberLedger extends BaseEntity {

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
            String memberNo,
            MemberLedgerEventType eventType,
            String statusCodeBefore,
            String statusCodeAfter,
            String snapshotJson
    ) {
        MemberLedger ledger = new MemberLedger();
        ledger.memberNo = memberNo;
        ledger.eventType = eventType;
        ledger.statusCodeBefore = statusCodeBefore;
        ledger.statusCodeAfter = statusCodeAfter;
        ledger.snapshotJson = snapshotJson;
        return ledger;
    }
}

