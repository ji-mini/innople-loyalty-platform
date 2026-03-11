package com.innople.loyalty.service.admin;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static com.innople.loyalty.service.admin.AdminAuthExceptions.AdminUserAlreadyExistsException;
import static com.innople.loyalty.service.admin.AdminAuthExceptions.InvalidCredentialsException;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminLoginResult login(String phoneNumber, String password) {
        UUID tenantId = TenantContext.requireTenantId();

        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        if (normalizedPhoneNumber == null || password == null || password.isBlank()) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        AdminUser admin = adminUserRepository
                .findByTenantIdAndPhoneNumber(tenantId, normalizedPhoneNumber)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        String storedHash = admin.getPasswordHash();
        if (!matches(password, storedHash)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (shouldUpgradeToBcrypt(storedHash)) {
            admin.changePasswordHash(passwordEncoder.encode(password));
            adminUserRepository.save(admin);
        }

        // NOTE: Security is not implemented yet. This token is for UI session only.
        String token = UUID.randomUUID().toString();
        AdminRole role = admin.getRole() != null ? admin.getRole() : AdminRole.OPERATOR;
        return new AdminLoginResult(admin.getId(), admin.getPhoneNumber(), admin.getEmail(), admin.getName(), role, token);
    }

    @Override
    @Transactional
    public AdminRegisterResult register(String phoneNumber, String email, String name, String password) {
        UUID tenantId = TenantContext.requireTenantId();

        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        if (normalizedPhoneNumber == null) {
            throw new IllegalArgumentException("phoneNumber must be a valid phone number");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }

        if (adminUserRepository.findByTenantIdAndPhoneNumber(tenantId, normalizedPhoneNumber).isPresent()) {
            throw new AdminUserAlreadyExistsException("admin phoneNumber already exists");
        }

        String normalizedEmail = normalizeEmailOrNull(email);
        if (normalizedEmail != null && adminUserRepository.findByTenantIdAndEmail(tenantId, normalizedEmail).isPresent()) {
            throw new AdminUserAlreadyExistsException("admin email already exists");
        }

        String passwordHash = passwordEncoder.encode(password);
        AdminUser admin = new AdminUser(normalizedPhoneNumber, normalizedEmail, name.trim(), passwordHash);
        try {
            AdminUser saved = adminUserRepository.save(admin);
            AdminRole role = saved.getRole() != null ? saved.getRole() : AdminRole.OPERATOR;
            return new AdminRegisterResult(saved.getId(), saved.getPhoneNumber(), saved.getEmail(), saved.getName(), role);
        } catch (DataIntegrityViolationException e) {
            throw new AdminUserAlreadyExistsException("admin user already exists");
        }
    }

    private String normalizeEmailOrNull(String rawEmail) {
        if (rawEmail == null) return null;
        String trimmed = rawEmail.trim();
        if (trimmed.isEmpty()) return null;
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String normalizePhoneNumber(String rawPhoneNumber) {
        if (rawPhoneNumber == null) return null;
        String trimmed = rawPhoneNumber.trim();
        if (trimmed.isEmpty()) return null;

        StringBuilder sb = new StringBuilder(trimmed.length());
        boolean plusSeen = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '+' && sb.length() == 0 && !plusSeen) {
                sb.append(c);
                plusSeen = true;
                continue;
            }
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }

        if (sb.length() == 0) return null;

        int digits = 0;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c >= '0' && c <= '9') digits++;
        }
        if (digits < 9 || digits > 15) return null;

        return sb.toString();
    }

    private boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null) return false;
        String s = storedHash.trim();

        if (s.startsWith("bcrypt:")) {
            String h = s.substring("bcrypt:".length()).trim();
            return looksLikeBcrypt(h) && passwordEncoder.matches(rawPassword, h);
        }
        if (s.startsWith("{bcrypt}")) {
            String h = s.substring("{bcrypt}".length()).trim();
            return looksLikeBcrypt(h) && passwordEncoder.matches(rawPassword, h);
        }

        if (looksLikeBcrypt(s)) {
            return passwordEncoder.matches(rawPassword, s);
        }

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

    private boolean shouldUpgradeToBcrypt(String storedHash) {
        if (storedHash == null) return false;
        String s = storedHash.trim();
        return !looksLikeBcrypt(s);
    }

    private boolean looksLikeBcrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}

