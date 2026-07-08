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
 * 회원 등급(membership_grade) 변경 이력.
 * 상태 변경 이력({@link MemberStatusHistory})과 별개로, 등급 전이만 독립적으로 기록한다.
 * 등급은 membership_grades FK(UUID)로 관리되므로 이력에도 from/to 등급 id를 UUID로 기록한다.
 */
@Entity
@Table(
        name = "member_grade_history",
        indexes = {
                @Index(name = "idx_member_grade_history_tenant_member", columnList = "tenantId,memberId,changedAt"),
                @Index(name = "idx_member_grade_history_tenant_changed_at", columnList = "tenantId,changedAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberGradeHistory {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, updatable = false)
    private UUID memberId;

    /** 등급을 변경한 관리자 id. 시스템 변경 등으로 알 수 없으면 null. */
    @Column(updatable = false)
    private UUID changedBy;

    @Column(updatable = false)
    private UUID fromGradeId;

    @Column(nullable = false, updatable = false)
    private UUID toGradeId;

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

    public static MemberGradeHistory of(
            UUID tenantId,
            UUID memberId,
            UUID changedBy,
            UUID fromGradeId,
            UUID toGradeId,
            String reason
    ) {
        MemberGradeHistory history = new MemberGradeHistory();
        history.tenantId = tenantId;
        history.memberId = memberId;
        history.changedBy = changedBy;
        history.fromGradeId = fromGradeId;
        history.toGradeId = toGradeId;
        history.reason = (reason == null || reason.isBlank()) ? null : reason.trim();
        return history;
    }
}
