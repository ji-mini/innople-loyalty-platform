package com.innople.loyalty.service.admin;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static com.innople.loyalty.service.admin.AdminAuthExceptions.InvalidCredentialsException;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminLoginResult login(String email, String password) {
        UUID tenantId = TenantContext.requireTenantId();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        AdminUser admin = adminUserRepository
                .findByTenantIdAndEmail(tenantId, normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!matches(password, admin.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // NOTE: Security is not implemented yet. This token is for UI session only.
        String token = UUID.randomUUID().toString();
        return new AdminLoginResult(admin.getId(), admin.getEmail(), admin.getName(), token);
    }

    private boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null) return false;
        String s = storedHash.trim();

        if (s.startsWith("sha256:")) {
            String expected = s.substring("sha256:".length());
            return PasswordHash.sha256Hex(rawPassword).equalsIgnoreCase(expected);
        }

        if (s.length() == 64 && s.chars().allMatch(c -> Character.digit(c, 16) != -1)) {
            return PasswordHash.sha256Hex(rawPassword).equalsIgnoreCase(s);
        }

        // Legacy/dev fallback (plain compare)
        return rawPassword.equals(s);
    }
}

