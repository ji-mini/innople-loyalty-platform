package com.innople.loyalty.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 회원 상태 변경 이력.
 * 회원 상태(status_code)는 공통코드(MEMBER_STATUS 그룹) 기반 문자열이므로 이력도 문자열로 기록한다.
 */
@Entity
@Table(
        name = "member_status_history",
        indexes = {
                @Index(name = "idx_member_status_history_tenant_member", columnList = "tenantId,memberId,changedAt"),
                @Index(name = "idx_member_status_history_tenant_changed_at", columnList = "tenantId,changedAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberStatusHistory {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, updatable = false)
    private UUID memberId;

    /** 상태를 변경한 관리자 id. 시스템 변경 등으로 알 수 없으면 null. */
    @Column(updatable = false)
    private UUID changedBy;

    @Column(length = 50, updatable = false)
    private String fromStatus;

    @Column(nullable = false, length = 50, updatable = false)
    private String toStatus;

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

    public static MemberStatusHistory of(
            UUID tenantId,
            UUID memberId,
            UUID changedBy,
            String fromStatus,
            String toStatus,
            String reason
    ) {
        MemberStatusHistory history = new MemberStatusHistory();
        history.tenantId = tenantId;
        history.memberId = memberId;
        history.changedBy = changedBy;
        history.fromStatus = fromStatus;
        history.toStatus = toStatus;
        history.reason = (reason == null || reason.isBlank()) ? null : reason.trim();
        return history;
    }
}
