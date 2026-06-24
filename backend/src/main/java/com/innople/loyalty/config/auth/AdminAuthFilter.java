package com.innople.loyalty.config.auth;

import com.innople.loyalty.config.ApiAuditLogInterceptor;
import com.innople.loyalty.config.TenantContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 어드민 콘솔 API(/api/v1/admin/**)의 JWT 검증을 담당합니다.
 * 토큰이 없거나 만료/위조된 경우 401을 반환하여 서버 차원에서 세션을 강제 종료합니다.
 * 토큰 발급 전 단계인 로그인/회원가입 경로는 검증 대상에서 제외합니다.
 */
@Component
public class AdminAuthFilter extends OncePerRequestFilter {
    private final AdminJwtTokenProvider adminJwtTokenProvider;

    public AdminAuthFilter(AdminJwtTokenProvider adminJwtTokenProvider) {
        this.adminJwtTokenProvider = adminJwtTokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return true;
        }
        // 토큰 발급 전이므로 인증 제외
        if (uri.equals("/api/v1/admin/auth/login") || uri.equals("/api/v1/admin/auth/register")) {
            return true;
        }
        // 그 외 모든 어드민 API는 검증 (refresh 포함)
        return !uri.startsWith("/api/v1/admin/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Missing bearer token");
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            writeUnauthorized(response, "Invalid bearer token");
            return;
        }

        try {
            AdminJwtTokenProvider.AdminJwtClaims claims = adminJwtTokenProvider.parseAndValidate(token);
            UUID contextTenantId = TenantContext.requireTenantId();
            if (!contextTenantId.equals(claims.tenantId())) {
                writeUnauthorized(response, "Token tenant does not match request tenant");
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.adminUserId().toString(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("adminUserId", claims.adminUserId());
            request.setAttribute("adminRole", claims.role());
            request.setAttribute(ApiAuditLogInterceptor.AUTHENTICATED_ADMIN_USER_ID_ATTRIBUTE, claims.adminUserId());
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException | JwtException ex) {
            writeUnauthorized(response, "Invalid or expired token");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
