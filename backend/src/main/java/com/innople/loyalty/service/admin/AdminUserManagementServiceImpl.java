package com.innople.loyalty.service.admin;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.domain.user.AdminUserStatus;
import com.innople.loyalty.repository.AdminUserRepository;
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

@Service
@RequiredArgsConstructor
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final AdminUserRepository adminUserRepository;
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
    public AdminUserItem updateStatus(UUID adminUserId, AdminUserStatus status) {
        UUID tenantId = TenantContext.requireTenantId();
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (status != AdminUserStatus.ACTIVE && status != AdminUserStatus.INACTIVE) {
            throw new IllegalArgumentException("status must be ACTIVE or INACTIVE");
        }
        AdminUser adminUser = adminUserRepository.findByTenantIdAndId(tenantId, adminUserId)
                .orElseThrow(() -> new AdminUserNotFoundException("admin user not found"));

        adminUser.changeStatus(status);
        return toItem(adminUserRepository.save(adminUser));
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

