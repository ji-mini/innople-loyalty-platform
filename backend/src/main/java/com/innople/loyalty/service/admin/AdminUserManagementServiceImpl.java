package com.innople.loyalty.service.admin;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.domain.user.AdminUserStatus;
import com.innople.loyalty.domain.user.AdminUserStatusHistory;
import com.innople.loyalty.repository.AdminUserRepository;
import com.innople.loyalty.repository.AdminUserStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.innople.loyalty.service.admin.AdminAuthExceptions.AdminUserAlreadyExistsException;
import static com.innople.loyalty.service.admin.AdminUserManagementExceptions.AdminUserNotFoundException;
import static com.innople.loyalty.service.admin.AdminUserManagementExceptions.InvalidAdminUserPhoneNumberException;
import static com.innople.loyalty.service.admin.AdminUserManagementExceptions.InvalidAdminUserStatusTransitionException;

@Service
@RequiredArgsConstructor
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final AdminUserRepository adminUserRepository;
    private final AdminUserStatusHistoryRepository adminUserStatusHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserItem> list(String keyword) {
        UUID tenantId = TenantContext.requireTenantId();
        String k = (keyword == null) ? null : keyword.trim();
        return adminUserRepository.searchByTenantId(tenantId, k).stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    @Transactional
    public AdminUserItem create(String phoneNumber, String email, String name, String password, AdminRole role) {
        TenantContext.requireTenantId();
        String p = normalizePhoneNumber(phoneNumber);
        String e = normalizeEmail(email);
        String n = (name == null) ? "" : name.trim();
        if (n.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }
        AdminRole r = (role == null) ? AdminRole.OPERATOR : role;

        AdminUser adminUser = new AdminUser(p, e, n, passwordEncoder.encode(password));
        adminUser.changeRole(r);
        // SUPER_ADMIN이 직접 등록하는 계정은 승인 절차 없이 즉시 활성화한다.
        adminUser.changeStatus(AdminUserStatus.ACTIVE);
        try {
            AdminUser saved = adminUserRepository.save(adminUser);
            return toItem(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AdminUserAlreadyExistsException("admin user already exists");
        }
    }

    @Override
    @Transactional
    public AdminUserItem update(UUID adminUserId, String phoneNumber, String email, String name, AdminRole role) {
        UUID tenantId = TenantContext.requireTenantId();
        AdminUser adminUser = adminUserRepository.findByTenantIdAndId(tenantId, adminUserId)
                .orElseThrow(() -> new AdminUserNotFoundException("admin user not found"));

        String p = normalizePhoneNumber(phoneNumber);
        String e = normalizeEmail(email);
        String n = (name == null) ? "" : name.trim();
        if (n.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        AdminRole r = (role == null) ? AdminRole.OPERATOR : role;

        adminUser.changeProfile(p, e, n);
        adminUser.changeRole(r);

        try {
            return toItem(adminUserRepository.save(adminUser));
        } catch (DataIntegrityViolationException ex) {
            throw new AdminUserAlreadyExistsException("admin user already exists");
        }
    }

    @Override
    @Transactional
    public AdminUserItem updateStatus(UUID adminUserId, AdminUserStatus status, UUID changedBy, String reason) {
        UUID tenantId = TenantContext.requireTenantId();
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        AdminUser adminUser = adminUserRepository.findByTenantIdAndId(tenantId, adminUserId)
                .orElseThrow(() -> new AdminUserNotFoundException("admin user not found"));

        AdminUserStatus from = adminUser.getStatus();
        validateTransition(from, status);

        adminUser.changeStatus(status);
        AdminUser saved = adminUserRepository.save(adminUser);

        adminUserStatusHistoryRepository.save(
                AdminUserStatusHistory.of(tenantId, adminUserId, changedBy, from, status, reason)
        );

        return toItem(saved);
    }

    /**
     * 어드민 계정 상태 전이 규칙.
     * - PENDING → ACTIVE(승인) / REJECTED(거절)
     * - ACTIVE → INACTIVE(정지)
     * - INACTIVE → ACTIVE(재활성화)
     * - REJECTED → (전이 불가, 재승인 불가)
     * 그 외 전이는 400으로 거부한다.
     */
    private void validateTransition(AdminUserStatus from, AdminUserStatus to) {
        if (from == null) {
            throw new InvalidAdminUserStatusTransitionException("현재 상태를 확인할 수 없어 상태를 변경할 수 없습니다.");
        }
        if (from == to) {
            throw new InvalidAdminUserStatusTransitionException(
                    "이미 " + statusLabel(to) + " 상태입니다.");
        }
        boolean allowed = switch (from) {
            case PENDING -> to == AdminUserStatus.ACTIVE || to == AdminUserStatus.REJECTED;
            case ACTIVE -> to == AdminUserStatus.INACTIVE;
            case INACTIVE -> to == AdminUserStatus.ACTIVE;
            case REJECTED -> false;
        };
        if (!allowed) {
            throw new InvalidAdminUserStatusTransitionException(
                    statusLabel(from) + " 상태에서 " + statusLabel(to) + " 상태로 변경할 수 없습니다.");
        }
    }

    private String statusLabel(AdminUserStatus status) {
        return switch (status) {
            case PENDING -> "승인대기(PENDING)";
            case ACTIVE -> "활성(ACTIVE)";
            case INACTIVE -> "비활성(INACTIVE)";
            case REJECTED -> "거절(REJECTED)";
        };
    }

    private AdminUserItem toItem(AdminUser a) {
        return new AdminUserItem(
                a.getId(),
                a.getTenantId(),
                a.getPhoneNumber(),
                a.getEmail(),
                a.getName(),
                (a.getRole() == null) ? AdminRole.OPERATOR : a.getRole(),
                (a.getStatus() == null) ? AdminUserStatus.PENDING : a.getStatus(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String raw = (phoneNumber == null) ? "" : phoneNumber.trim();
        String digits = raw.replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new InvalidAdminUserPhoneNumberException("phoneNumber must not be blank");
        }
        if (digits.length() > 20) {
            throw new InvalidAdminUserPhoneNumberException("phoneNumber is too long");
        }
        return digits;
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        String v = email.trim();
        if (v.isBlank()) return null;
        return v.toLowerCase();
    }
}

