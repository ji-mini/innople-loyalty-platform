package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.log.ApiAuditCategory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class LogDtos {
    private LogDtos() {
    }

    public record ApiAuditLogItemResponse(
            UUID id,
            ApiAuditCategory category,
            String httpMethod,
            String path,
            String queryString,
            int statusCode,
            long durationMs,
            UUID adminUserId,
            String ip,
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

