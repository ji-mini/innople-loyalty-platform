package com.innople.loyalty.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 요청 헤더(X-Tenant-Id)로부터 테넌트 컨텍스트를 해석해 {@link TenantContext}에 세팅한다.
 *
 * <p>반드시 Spring Security 필터체인(그 안의 AdminAuthFilter/MemberAuthFilter)보다 <b>먼저</b>
 * 실행되어야 한다. 두 인증 필터가 {@code TenantContext.requireTenantId()}로 JWT의 테넌트와
 * 요청 테넌트 일치를 검증하기 때문이다. 따라서 {@link Ordered#HIGHEST_PRECEDENCE}로 등록한다.
 * (Spring Security 필터체인 기본 order는 -100이므로 그보다 앞선다.)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextFilter extends OncePerRequestFilter {

    private final String tenantHeaderName;
    private final List<String> allowedOrigins;
    private final boolean allowCredentials;

    public TenantContextFilter(
            @Value("${app.tenant.header-name:X-Tenant-Id}") String tenantHeaderName,
            @Value("${app.cors.allowed-origins:http://localhost:8081,http://127.0.0.1:8081,http://localhost:8090}") String allowedOrigins,
            @Value("${app.cors.allow-credentials:false}") boolean allowCredentials
    ) {
        this.tenantHeaderName = tenantHeaderName;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        this.allowCredentials = allowCredentials;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if ("/error".equals(uri)) {
            return true;
        }
        if (isSwaggerPath(uri)) {
            return true;
        }
        return uri != null && uri.startsWith("/api/v1/public/");
    }

    private boolean isSwaggerPath(String uri) {
        return uri != null
                && (uri.startsWith("/swagger-ui/")
                || uri.equals("/swagger-ui.html")
                || uri.equals("/v3/api-docs")
                || uri.startsWith("/v3/api-docs/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String rawTenantId = request.getHeader(tenantHeaderName);
        if (rawTenantId == null || rawTenantId.isBlank()) {
            writeBadRequest(request, response, "Missing tenant header: " + tenantHeaderName);
            return;
        }

        try {
            UUID tenantId = UUID.fromString(rawTenantId.trim());
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException e) {
            writeBadRequest(request, response, "Invalid tenant header UUID: " + tenantHeaderName);
        } finally {
            TenantContext.clear();
        }
    }

    private void writeBadRequest(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {
        // 이 필터는 Spring Security의 CorsFilter보다 먼저 실행되므로,
        // 여기서 직접 에러 응답을 내보낼 때 CORS 헤더를 수동으로 보정해
        // 브라우저가 400을 CORS 에러로 오인하지 않도록 한다.
        applyCorsHeaders(request, response);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }

    private void applyCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        if (origin == null || !allowedOrigins.contains(origin)) {
            return;
        }
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Vary", "Origin");
        if (allowCredentials) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
        }
    }
}
