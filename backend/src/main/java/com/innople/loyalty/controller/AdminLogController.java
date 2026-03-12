package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.LogDtos;
import com.innople.loyalty.domain.log.ApiAuditCategory;
import com.innople.loyalty.service.log.ApiAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/admin/logs")
@RequiredArgsConstructor
public class AdminLogController {

    private final ApiAuditLogService apiAuditLogService;

    @GetMapping
    public LogDtos.PagedResponse<LogDtos.ApiAuditLogItemResponse> list(
            @RequestParam ApiAuditCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toAt,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        ApiAuditLogService.PagedResult result = apiAuditLogService.list(category, fromAt, toAt, keyword, page, size);
        return new LogDtos.PagedResponse<>(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }

    private LogDtos.ApiAuditLogItemResponse toResponse(ApiAuditLogService.ApiAuditLogItem i) {
        return new LogDtos.ApiAuditLogItemResponse(
                i.id(),
                i.category(),
                i.httpMethod(),
                i.path(),
                i.queryString(),
                i.statusCode(),
                i.durationMs(),
                i.adminUserId(),
                i.ip(),
                i.createdAt()
        );
    }
}

