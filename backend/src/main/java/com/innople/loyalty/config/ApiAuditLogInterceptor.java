package com.innople.loyalty.config;

import com.innople.loyalty.domain.log.ApiAuditCategory;
import com.innople.loyalty.service.log.ApiAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiAuditLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = ApiAuditLogInterceptor.class.getName() + ".START_TIME";

    private final ApiAuditLogService apiAuditLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String uri = request.getRequestURI();
        if (shouldSkip(uri, request.getMethod())) {
            return;
        }

        Long startedAt = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long durationMs = (startedAt == null) ? 0L : Math.max(System.currentTimeMillis() - startedAt, 0L);
        UUID tenantId = parseUuidHeader(request.getHeader("X-Tenant-Id"));
        if (tenantId == null) {
            return;
        }

        ApiAuditCategory category = (uri != null && uri.startsWith("/api/v1/points/"))
                ? ApiAuditCategory.POINT_API
                : ApiAuditCategory.ADMIN_USAGE;

        try {
            TenantContext.setTenantId(tenantId);
            apiAuditLogService.write(
                    category,
                    request.getMethod(),
                    uri,
                    request.getQueryString(),
                    response.getStatus(),
                    durationMs,
                    parseUuidHeader(request.getHeader("X-Admin-User-Id")),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );
        } catch (RuntimeException writeException) {
            log.warn("Failed to write api audit log. uri={}", uri, writeException);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean shouldSkip(String uri, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if (uri == null || "/error".equals(uri)) {
            return true;
        }
        if (!uri.startsWith("/api/v1/")) {
            return true;
        }
        return uri.startsWith("/api/v1/public/");
    }

    private UUID parseUuidHeader(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
