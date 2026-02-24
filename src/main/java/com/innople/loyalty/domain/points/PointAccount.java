package com.innople.loyalty.domain.points;

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
        name = "point_accounts",
        indexes = {
                @Index(name = "idx_point_accounts_tenant_id", columnList = "tenantId"),
                @Index(name = "idx_point_accounts_tenant_member", columnList = "tenantId,memberId", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointAccount extends BaseEntity {

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private long currentBalance;

    @Version
    @Column(nullable = false)
    private long version;

    public PointAccount(UUID memberId) {
        this.memberId = memberId;
        this.currentBalance = 0L;
    }

    public void addBalance(long delta) {
        this.currentBalance = Math.addExact(this.currentBalance, delta);
    }
}

