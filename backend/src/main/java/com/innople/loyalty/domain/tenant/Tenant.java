package com.innople.loyalty.domain.tenant;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Tenant extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    // 대표코드(2자리). DB 수준의 NOT NULL 강제는 운영 마이그레이션 단계에서 적용하고,
    // 앱 레벨에서는 항상 값이 존재하도록 초기화/검증/백필 처리합니다.
    @Column(length = 2)
    private String representativeCode;

    public Tenant(String name) {
        this.name = name;
    }

    public Tenant(String name, String representativeCode) {
        this.name = name;
        this.representativeCode = normalizeRepresentativeCode(representativeCode);
    }

    public void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name.trim();
    }

    public void changeRepresentativeCode(String representativeCode) {
        this.representativeCode = normalizeRepresentativeCode(representativeCode);
    }

    private String normalizeRepresentativeCode(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("representativeCode must not be blank");
        }
        String v = raw.trim().toUpperCase();
        if (v.length() != 2) {
            throw new IllegalArgumentException("representativeCode must be exactly 2 letters");
        }
        char a = v.charAt(0);
        char b = v.charAt(1);
        if (a < 'A' || a > 'Z' || b < 'A' || b > 'Z') {
            throw new IllegalArgumentException("representativeCode must be A-Z letters");
        }
        return v;
    }

    @Override
    protected boolean allowSelfTenantIdWhenContextMissing() {
        return true;
    }
}

