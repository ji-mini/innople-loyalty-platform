package com.innople.loyalty.domain.common;

import com.innople.loyalty.config.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        UUID contextTenantId = TenantContext.getTenantId().orElse(null);
        if (tenantId == null) {
            if (contextTenantId == null) {
                if (allowSelfTenantIdWhenContextMissing()) {
                    tenantId = id;
                    return;
                }
                throw new TenantMismatchException("tenantId is missing and TenantContext is not set");
            }
            tenantId = contextTenantId;
        } else if (contextTenantId != null && !tenantId.equals(contextTenantId)) {
            throw new TenantMismatchException("tenantId mismatch with TenantContext");
        }
    }

    @PreUpdate
    protected void preUpdate() {
        UUID contextTenantId = TenantContext.getTenantId().orElse(null);
        if (contextTenantId != null && !tenantId.equals(contextTenantId)) {
            throw new TenantMismatchException("tenantId mismatch with TenantContext");
        }
    }

    protected boolean allowSelfTenantIdWhenContextMissing() {
        return false;
    }
}

