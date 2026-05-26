package com.innople.loyalty.domain.member;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "member_credentials",
        indexes = {
                @Index(name = "idx_member_credentials_tenant_id", columnList = "tenantId"),
                @Index(name = "uk_member_credentials_tenant_member", columnList = "tenantId,memberId", unique = true),
                @Index(name = "idx_member_credentials_tenant_phone", columnList = "tenantId,phoneNumber")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCredential extends BaseEntity {

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean deleted;

    private MemberCredential(UUID memberId, String phoneNumber, String email, String passwordHash, boolean deleted) {
        this.memberId = memberId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
        this.deleted = deleted;
    }

    public static MemberCredential of(UUID memberId, String phoneNumber, String email, String passwordHash) {
        if (memberId == null) {
            throw new IllegalArgumentException("memberId must not be null");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        return new MemberCredential(
                memberId,
                phoneNumber.trim(),
                normalizeEmailOrNull(email),
                passwordHash.trim(),
                false
        );
    }

    public void enable(String phoneNumber, String email, String passwordHash) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
        this.phoneNumber = phoneNumber.trim();
        this.email = normalizeEmailOrNull(email);
        this.passwordHash = passwordHash.trim();
        this.deleted = false;
    }

    public void changePasswordHash(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("newPasswordHash must not be blank");
        }
        this.passwordHash = newPasswordHash.trim();
    }

    public void updateProfile(String phoneNumber, String email) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        this.phoneNumber = phoneNumber.trim();
        this.email = normalizeEmailOrNull(email);
    }

    public void disable() {
        this.deleted = true;
    }

    private static String normalizeEmailOrNull(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
