package com.innople.loyalty.config;

import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.repository.AdminUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminRoleResolver {

    private static final String DEFAULT_ADMIN_USER_HEADER = "X-Admin-User-Id";

    private final AdminUserRepository adminUserRepository;

    @Value("${app.admin-user.header-name:X-Admin-User-Id}")
    private String adminUserHeaderName;

    /**
     * Resolves AdminUser from request and ensures role is at least the required level.
     * Throws AdminAccessDeniedException if admin not found or role insufficient.
     */
    public void requireAtLeast(HttpServletRequest request, AdminRole required) {
        AdminUser admin = resolve(request);
        if (admin == null) {
            throw new AdminAccessDeniedException("관리자 인증이 필요합니다.");
        }
        AdminRole role = admin.getRole() != null ? admin.getRole() : AdminRole.OPERATOR;
        if (!role.atLeast(required)) {
            throw new AdminAccessDeniedException("권한이 부족합니다. " + required + " 이상 필요합니다.");
        }
    }

    public AdminUser resolve(HttpServletRequest request) {
        UUID adminUserId = resolveAdminUserId(request);
        if (adminUserId == null) {
            return null;
        }
        try {
            UUID tenantId = TenantContext.requireTenantId();
            return adminUserRepository.findByTenantIdAndId(tenantId, adminUserId).orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * JWT 검증 필터(AdminAuthFilter)가 설정한 검증된 adminUserId를 우선 사용하고,
     * 없을 경우에만 클라이언트 헤더로 폴백한다(위변조 방지).
     */
    private UUID resolveAdminUserId(HttpServletRequest request) {
        Object verified = request.getAttribute("adminUserId");
        if (verified instanceof UUID uuid) {
            return uuid;
        }
        String raw = request.getHeader(adminUserHeaderName);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static class AdminAccessDeniedException extends RuntimeException {
        public AdminAccessDeniedException(String message) {
            super(message);
        }
    }
}
