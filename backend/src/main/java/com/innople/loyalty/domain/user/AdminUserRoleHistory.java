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
 * 어드민 계정 권한(role) 변경 이력.
 * 상태 변경 이력({@link AdminUserStatusHistory})과 별개로, role 전이만 독립적으로 기록한다.
 * 승인 시점에 role이 함께 바뀌는 경우와, 이미 ACTIVE인 계정의 role만 단독으로 바뀌는 경우 모두 기록 대상이다.
 */
@Entity
@Table(
        name = "admin_user_role_history",
        indexes = {
                @Index(name = "idx_admin_user_role_history_tenant_admin", columnList = "tenantId,adminUserId,changedAt"),
                @Index(name = "idx_admin_user_role_history_tenant_changed_at", columnList = "tenantId,changedAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUserRoleHistory {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, updatable = false)
    private UUID adminUserId;

    /** 권한을 변경한 관리자(SUPER_ADMIN) id. 시스템 변경 등으로 알 수 없으면 null. */
    @Column(updatable = false)
    private UUID changedBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, updatable = false)
    private AdminRole fromRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private AdminRole toRole;

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

    public static AdminUserRoleHistory of(
            UUID tenantId,
            UUID adminUserId,
            UUID changedBy,
            AdminRole fromRole,
            AdminRole toRole,
            String reason
    ) {
        AdminUserRoleHistory history = new AdminUserRoleHistory();
        history.tenantId = tenantId;
        history.adminUserId = adminUserId;
        history.changedBy = changedBy;
        history.fromRole = fromRole;
        history.toRole = toRole;
        history.reason = (reason == null || reason.isBlank()) ? null : reason.trim();
        return history;
    }
}
