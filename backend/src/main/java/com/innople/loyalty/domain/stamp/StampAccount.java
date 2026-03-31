package com.innople.loyalty.domain.stamp;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "stamp_accounts",
        indexes = {
                @Index(name = "idx_stamp_accounts_tenant_id", columnList = "tenantId"),
                @Index(name = "uk_stamp_accounts_tenant_member", columnList = "tenantId,memberId", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StampAccount extends BaseEntity {

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false, name = "current_balance")
    private int currentBalance;

    @Version
    @Column(nullable = false)
    private long version;

    public StampAccount(UUID memberId) {
        this.memberId = memberId;
        this.currentBalance = 0;
    }

    public void addStamps(int delta) {
        this.currentBalance = Math.addExact(this.currentBalance, delta);
        if (this.currentBalance < 0) {
            throw new IllegalStateException("stamp balance would become negative");
        }
    }
}
