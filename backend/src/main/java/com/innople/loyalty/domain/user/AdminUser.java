package com.innople.loyalty.domain.user;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "admin_users",
        indexes = {
                @Index(name = "idx_admin_users_tenant_id", columnList = "tenantId"),
                @Index(name = "idx_admin_users_tenant_email", columnList = "tenantId,email", unique = true),
                @Index(name = "idx_admin_users_tenant_phone_number", columnList = "tenantId,phoneNumber", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUser extends BaseEntity {

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = true, length = 200)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 30)
    private AdminRole role;

    public AdminUser(String phoneNumber, String email, String name, String passwordHash) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    void prePersistAdminUser() {
        if (role == null) {
            role = AdminRole.OPERATOR;
        }
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void changeRole(AdminRole role) {
        this.role = role;
    }
}

