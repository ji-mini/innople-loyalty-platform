package com.innople.loyalty.domain.batch;

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
 * 배치 1회 실행이 각 테넌트를 처리할 때마다 테넌트별로 1행 기록되는 실행 이력.
 * "오늘 이 테넌트가 이미 처리됐는가"(catch-up) 판정에도 조회한다.
 *
 * <p>V32 스키마(batch_execution_history)에 매핑한다. 이 테이블은 updated_at 이 없으므로
 * (감사 컬럼 세트가 다르다) {@code BaseEntity} 를 상속하지 않고 {@code MemberStatusHistory} 와 동일하게
 * 독립 매핑한다. tenant_id 는 서비스에서 명시적으로 세팅한다.</p>
 */
@Entity
@Table(
        name = "batch_execution_history",
        indexes = {
                @Index(name = "idx_batch_execution_history_tenant_batch_started", columnList = "tenantId,batchName,startedAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchExecutionHistory {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 4000;

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100, updatable = false)
    private String batchName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchExecutionStatus status;

    @Column(nullable = false, updatable = false)
    private Instant startedAt;

    @Column
    private Instant finishedAt;

    @Column(nullable = false)
    private int processedCount;

    @Column(nullable = false)
    private int errorCount;

    @Column(columnDefinition = "text")
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (startedAt == null) {
            startedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        if (status == null) {
            status = BatchExecutionStatus.RUNNING;
        }
    }

    /** 실행 시작 이력(RUNNING). started_at 은 현재 시각. */
    public static BatchExecutionHistory start(UUID tenantId, String batchName) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        BatchExecutionHistory history = new BatchExecutionHistory();
        history.tenantId = tenantId;
        history.batchName = requireText(batchName);
        history.status = BatchExecutionStatus.RUNNING;
        history.startedAt = Instant.now();
        history.processedCount = 0;
        history.errorCount = 0;
        return history;
    }

    /** 실행 종료 시 결과를 반영한다. finished_at 은 현재 시각. */
    public void finish(BatchExecutionStatus status, int processedCount, int errorCount, String errorMessage) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        this.status = status;
        this.processedCount = Math.max(processedCount, 0);
        this.errorCount = Math.max(errorCount, 0);
        this.errorMessage = truncate(errorMessage);
        this.finishedAt = Instant.now();
    }

    private static String truncate(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String trimmed = message.trim();
        return trimmed.length() > ERROR_MESSAGE_MAX_LENGTH
                ? trimmed.substring(0, ERROR_MESSAGE_MAX_LENGTH)
                : trimmed;
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("batchName must not be blank");
        }
        return value.trim();
    }
}
