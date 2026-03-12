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

    public Tenant(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name.trim();
    }

    @Override
    protected boolean allowSelfTenantIdWhenContextMissing() {
        return true;
    }
}

