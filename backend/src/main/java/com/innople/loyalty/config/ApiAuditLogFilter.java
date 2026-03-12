package com.innople.loyalty.config;

import com.innople.loyalty.domain.log.ApiAuditCategory;
import com.innople.loyalty.service.log.ApiAuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class ApiAuditLogFilter extends OncePerRequestFilter {

    private final ApiAuditLogService apiAuditLogService;

    public ApiAuditLogFilter(ApiAuditLogService apiAuditLogService) {
        this.apiAuditLogService = apiAuditLogService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String uri = request.getRequestURI();
        if (uri == null) return true;
        if ("/error".equals(uri)) return true;
        if (!uri.startsWith("/api/v1/")) return true;
        return uri.startsWith("/api/v1/public/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        StatusCaptureResponseWrapper resp = new StatusCaptureResponseWrapper(response);
        try {
            filterChain.doFilter(request, resp);
        } finally {
            long durationMs = Math.max(System.currentTimeMillis() - start, 0);
            String uri = request.getRequestURI();
            ApiAuditCategory category = (uri != null && uri.startsWith("/api/v1/points/"))
                    ? ApiAuditCategory.POINT_API
                    : ApiAuditCategory.ADMIN_USAGE;

            UUID adminUserId = parseUuidHeader(request.getHeader("X-Admin-User-Id"));
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");

            try {
                apiAuditLogService.write(
                        category,
                        request.getMethod(),
                        uri,
                        request.getQueryString(),
                        resp.getStatus(),
                        durationMs,
                        adminUserId,
                        ip,
                        ua
                );
            } catch (RuntimeException ignored) {
                // Do not break actual API flow when logging fails.
            }
        }
    }

    private UUID parseUuidHeader(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final class StatusCaptureResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private int status = HttpServletResponse.SC_OK;

        private StatusCaptureResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.status = sc;
            super.sendError(sc);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.status = sc;
            super.sendError(sc, msg);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.status = HttpServletResponse.SC_FOUND;
            super.sendRedirect(location);
        }

        public int getStatus() {
            return status;
        }
    }
}

