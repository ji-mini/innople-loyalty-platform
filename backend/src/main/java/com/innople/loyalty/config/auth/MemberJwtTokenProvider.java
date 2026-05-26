package com.innople.loyalty.config.auth;

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

@Component
public class MemberJwtTokenProvider {
    private final SecretKey secretKey;
    private final long expirationSeconds;

    public MemberJwtTokenProvider(
            @Value("${app.auth.jwt.secret}") String jwtSecret,
            @Value("${app.auth.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalArgumentException("app.auth.jwt.secret must be at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    public String createAccessToken(UUID tenantId, UUID memberId, String loginId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(memberId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("loginId", loginId)
                .claim("tokenType", "MEMBER_ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public MemberJwtClaims parseAndValidate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String tokenType = claims.get("tokenType", String.class);
        if (!"MEMBER_ACCESS".equals(tokenType)) {
            throw new IllegalArgumentException("Invalid member access token");
        }
        UUID memberId = UUID.fromString(claims.getSubject());
        UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
        String loginId = claims.get("loginId", String.class);
        return new MemberJwtClaims(memberId, tenantId, loginId);
    }

    public record MemberJwtClaims(UUID memberId, UUID tenantId, String loginId) {
    }
}
