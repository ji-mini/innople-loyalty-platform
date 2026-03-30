package com.innople.loyalty.service.log;

import com.innople.loyalty.domain.log.ApiAuditCategory;

import java.time.Instant;
import java.util.UUID;

public interface ApiAuditLogService {
    PagedResult list(ApiAuditCategory category, Instant fromAt, Instant toAt, String keyword, int page, int size);

    void write(
            ApiAuditCategory category,
            String method,
            String path,
            String queryString,
            int statusCode,
            long durationMs,
            UUID adminUserId,
            String ip,
            String userAgent,
            String message
    );

    record ApiAuditLogItem(
            UUID id,
            ApiAuditCategory category,
            String httpMethod,
            String path,
            String queryString,
            int statusCode,
            long durationMs,
            UUID adminUserId,
            String ip,
            String userAgent,
            Instant createdAt,
            String displayMessage
    ) {
    }

    record PagedResult(
            java.util.List<ApiAuditLogItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}

