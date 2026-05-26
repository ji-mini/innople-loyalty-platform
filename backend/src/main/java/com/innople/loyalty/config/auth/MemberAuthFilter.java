package com.innople.loyalty.config.auth;

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

@Component
public class MemberAuthFilter extends OncePerRequestFilter {
    private final MemberJwtTokenProvider memberJwtTokenProvider;

    public MemberAuthFilter(MemberJwtTokenProvider memberJwtTokenProvider) {
        this.memberJwtTokenProvider = memberJwtTokenProvider;
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
        if (uri.startsWith("/api/v1/auth/")) {
            return true;
        }
        return !uri.equals("/api/v1/members/me") && !uri.startsWith("/api/v1/members/me/");
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
            MemberJwtTokenProvider.MemberJwtClaims claims = memberJwtTokenProvider.parseAndValidate(token);
            UUID contextTenantId = TenantContext.requireTenantId();
            if (!contextTenantId.equals(claims.tenantId())) {
                writeUnauthorized(response, "Token tenant does not match request tenant");
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.memberId().toString(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("memberId", claims.memberId());
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
