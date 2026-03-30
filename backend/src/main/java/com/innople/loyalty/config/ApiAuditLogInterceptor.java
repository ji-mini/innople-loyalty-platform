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
    public static final String AUTHENTICATED_ADMIN_USER_ID_ATTRIBUTE = ApiAuditLogInterceptor.class.getName() + ".AUTHENTICATED_ADMIN_USER_ID";
    /** 컨트롤러에서 설정하면 감사 로그 message에 저장됩니다. */
    public static final String AUDIT_MESSAGE_ATTRIBUTE = ApiAuditLogInterceptor.class.getName() + ".AUDIT_MESSAGE";

    public static void setAuditMessage(HttpServletRequest request, String message) {
        if (request == null || message == null || message.isBlank()) {
            return;
        }
        request.setAttribute(AUDIT_MESSAGE_ATTRIBUTE, message.trim());
    }

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
            UUID adminUserId = resolveAdminUserId(request);
            Object msgAttr = request.getAttribute(AUDIT_MESSAGE_ATTRIBUTE);
            String auditMessage = msgAttr instanceof String s && !s.isBlank() ? s : null;
            request.removeAttribute(AUDIT_MESSAGE_ATTRIBUTE);
            apiAuditLogService.write(
                    category,
                    request.getMethod(),
                    uri,
                    request.getQueryString(),
                    response.getStatus(),
                    durationMs,
                    adminUserId,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    auditMessage
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

    private UUID resolveAdminUserId(HttpServletRequest request) {
        Object authenticatedAdminUserId = request.getAttribute(AUTHENTICATED_ADMIN_USER_ID_ATTRIBUTE);
        if (authenticatedAdminUserId instanceof UUID uuid) {
            return uuid;
        }
        return parseUuidHeader(request.getHeader("X-Admin-User-Id"));
    }
}
