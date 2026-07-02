package com.innople.loyalty.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 어드민 계정 상태 변경 이력.
 * 승인/거절/정지/재활성화 등 모든 상태 전이를 기록한다.
 */
@Entity
@Table(
        name = "admin_user_status_history",
        indexes = {
                @Index(name = "idx_admin_user_status_history_tenant_admin", columnList = "tenantId,adminUserId,changedAt"),
                @Index(name = "idx_admin_user_status_history_tenant_changed_at", columnList = "tenantId,changedAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUserStatusHistory {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, updatable = false)
    private UUID adminUserId;

    /** 상태를 변경한 관리자(SUPER_ADMIN) id. 시스템 변경 등으로 알 수 없으면 null. */
    @Column(updatable = false)
    private UUID changedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, updatable = false)
    private AdminUserStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private AdminUserStatus toStatus;

    @Column(length = 500, updatable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    private Instant changedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (changedAt == null) {
            changedAt = Instant.now();
        }
    }

    public static AdminUserStatusHistory of(
            UUID tenantId,
            UUID adminUserId,
            UUID changedBy,
            AdminUserStatus fromStatus,
            AdminUserStatus toStatus,
            String reason
    ) {
        AdminUserStatusHistory history = new AdminUserStatusHistory();
        history.tenantId = tenantId;
        history.adminUserId = adminUserId;
        history.changedBy = changedBy;
        history.fromStatus = fromStatus;
        history.toStatus = toStatus;
        history.reason = (reason == null || reason.isBlank()) ? null : reason.trim();
        return history;
    }
}
