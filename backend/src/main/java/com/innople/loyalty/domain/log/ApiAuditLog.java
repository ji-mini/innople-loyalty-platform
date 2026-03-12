package com.innople.loyalty.domain.log;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(
        name = "api_audit_logs",
        indexes = {
                @Index(name = "idx_api_audit_logs_tenant_created_at", columnList = "tenantId,createdAt"),
                @Index(name = "idx_api_audit_logs_tenant_category_created_at", columnList = "tenantId,category,createdAt")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiAuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApiAuditCategory category;

    @Column(nullable = false, length = 10)
    private String httpMethod;

    @Column(nullable = false, length = 300)
    private String path;

    @Column(length = 1000)
    private String queryString;

    @Column(nullable = false)
    private int statusCode;

    @Column(nullable = false)
    private long durationMs;

    @Column
    private UUID adminUserId;

    @Column(length = 45)
    private String ip;

    @Column(length = 400)
    private String userAgent;

    @Column(length = 500)
    private String message;

    public static ApiAuditLog of(
            ApiAuditCategory category,
            String httpMethod,
            String path,
            String queryString,
            int statusCode,
            long durationMs,
            UUID adminUserId,
            String ip,
            String userAgent,
            String message
    ) {
        ApiAuditLog log = new ApiAuditLog();
        log.category = category;
        log.httpMethod = httpMethod;
        log.path = path;
        log.queryString = queryString;
        log.statusCode = statusCode;
        log.durationMs = durationMs;
        log.adminUserId = adminUserId;
        log.ip = ip;
        log.userAgent = userAgent;
        log.message = message;
        return log;
    }
}

