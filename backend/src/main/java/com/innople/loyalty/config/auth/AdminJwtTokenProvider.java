package com.innople.loyalty.config.auth;

import com.innople.loyalty.domain.user.AdminRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * 어드민 콘솔 세션용 JWT 발급/검증을 담당합니다.
 * 회원(MemberJwtTokenProvider)과 별도의 토큰 타입/만료 정책을 사용합니다.
 */
@Component
public class AdminJwtTokenProvider {
    private static final String TOKEN_TYPE = "ADMIN_ACCESS";

    private final SecretKey secretKey;
    private final long expirationSeconds;

    public AdminJwtTokenProvider(
            @Value("${app.auth.admin-jwt.secret:${app.auth.jwt.secret}}") String jwtSecret,
            @Value("${app.auth.admin-jwt.expiration-seconds:1800}") long expirationSeconds
    ) {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("app.auth.admin-jwt.secret must be at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public String createAccessToken(UUID tenantId, UUID adminUserId, AdminRole role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(adminUserId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("role", role != null ? role.name() : null)
                .claim("tokenType", TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public AdminJwtClaims parseAndValidate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get("tokenType", String.class);
        if (!TOKEN_TYPE.equals(tokenType)) {
            throw new IllegalArgumentException("Invalid admin access token");
        }
        UUID adminUserId = UUID.fromString(claims.getSubject());
        UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
        String role = claims.get("role", String.class);
        return new AdminJwtClaims(adminUserId, tenantId, role);
    }

    public record AdminJwtClaims(UUID adminUserId, UUID tenantId, String role) {
    }
}
