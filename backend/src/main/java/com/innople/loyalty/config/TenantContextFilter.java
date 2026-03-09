package com.innople.loyalty.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private final String tenantHeaderName;

    public TenantContextFilter(@Value("${app.tenant.header-name:X-Tenant-Id}") String tenantHeaderName) {
        this.tenantHeaderName = tenantHeaderName;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String rawTenantId = request.getHeader(tenantHeaderName);
        if (rawTenantId == null || rawTenantId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Missing tenant header: " + tenantHeaderName + "\"}");
            return;
        }

        try {
            UUID tenantId = UUID.fromString(rawTenantId.trim());
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Invalid tenant header UUID: " + tenantHeaderName + "\"}");
        } finally {
            TenantContext.clear();
        }
    }
}

