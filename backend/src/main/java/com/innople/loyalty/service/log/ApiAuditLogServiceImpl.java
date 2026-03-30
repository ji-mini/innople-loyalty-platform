package com.innople.loyalty.service.log;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.log.ApiAuditCategory;
import com.innople.loyalty.domain.log.ApiAuditLog;
import com.innople.loyalty.repository.ApiAuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiAuditLogServiceImpl implements ApiAuditLogService {

    private final ApiAuditLogRepository apiAuditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResult list(ApiAuditCategory category, Instant fromAt, Instant toAt, String keyword, int page, int size) {
        UUID tenantId = TenantContext.requireTenantId();
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 200);
        PageRequest pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<ApiAuditLog> spec = buildSearchSpec(tenantId, category, fromAt, toAt, normalize(keyword));
        Page<ApiAuditLog> result = apiAuditLogRepository.findAll(spec, pageable);

        return new PagedResult(
                result.getContent().stream().map(this::toItem).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @SuppressWarnings("java:S4276")
    private Specification<ApiAuditLog> buildSearchSpec(UUID tenantId, ApiAuditCategory category, Instant fromAt, Instant toAt, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (fromAt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromAt));
            }
            if (toAt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toAt));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("path")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("message"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("userAgent"), "")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(
            ApiAuditCategory category,
            String method,
            String path,
            String queryString,
            int statusCode,
            long durationMs,
            UUID adminUserId,
            String ip,
            String userAgent,
            String message
    ) {
        TenantContext.requireTenantId();
        ApiAuditLog log = ApiAuditLog.of(
                category,
                safe(method, 10),
                safe(path, 300),
                safe(queryString, 1000),
                statusCode,
                durationMs,
                adminUserId,
                safe(ip, 45),
                safe(userAgent, 400),
                safe(message, 500)
        );
        apiAuditLogRepository.save(log);
    }

    private ApiAuditLogItem toItem(ApiAuditLog l) {
        return new ApiAuditLogItem(
                l.getId(),
                l.getCategory(),
                l.getHttpMethod(),
                l.getPath(),
                l.getQueryString(),
                l.getStatusCode(),
                l.getDurationMs(),
                l.getAdminUserId(),
                l.getIp(),
                l.getUserAgent(),
                l.getCreatedAt(),
                AdminAuditDescriptionFormatter.describe(l)
        );
    }

    private String normalize(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isBlank() ? null : t;
    }

    private String safe(String v, int maxLen) {
        if (v == null) return null;
        String t = v.trim();
        if (t.isEmpty()) return null;
        return t.length() <= maxLen ? t : t.substring(0, maxLen);
    }
}

