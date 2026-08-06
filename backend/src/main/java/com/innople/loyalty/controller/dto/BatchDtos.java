package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BatchDtos {
    private BatchDtos() {
    }

    public record BatchJobConfigResponse(
            UUID id,
            String batchName,
            boolean enabled,
            short runHour,
            Integer thresholdDays,
            Instant createdAt,
            Instant updatedAt,
            UUID updatedBy,
            // 해당 (tenant, batchName)의 실행 이력 중 가장 최근 started_at. 이력이 없으면 null.
            Instant lastExecutedAt
    ) {
    }

    public record CreateBatchJobConfigRequest(
            // 미지정 시 기본 AUTO_WITHDRAWAL 로 처리한다.
            String batchName,
            Boolean enabled,
            @NotNull @Min(0) @Max(23) Short runHour,
            /** 배치에 따라 미사용(null) 가능. 값이 있으면 &gt; 0. */
            @Positive Integer thresholdDays
    ) {
    }

    public record UpdateBatchJobConfigRequest(
            @NotNull Boolean enabled,
            @NotNull @Min(0) @Max(23) Short runHour,
            /** 배치에 따라 미사용(null) 가능. 값이 있으면 &gt; 0. */
            @Positive Integer thresholdDays
    ) {
    }

    public record BatchExecutionHistoryResponse(
            UUID id,
            String batchName,
            String status,
            Instant startedAt,
            Instant finishedAt,
            int processedCount,
            int errorCount,
            String errorMessage,
            Instant createdAt
    ) {
    }

    public record PagedResponse<T>(
            List<T> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
