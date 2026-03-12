package com.innople.loyalty.domain.code;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "common_codes",
        indexes = {
                @Index(name = "idx_common_codes_tenant_id", columnList = "tenantId"),
                @Index(name = "uk_common_codes_tenant_group_code", columnList = "tenantId,codeGroup,code", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCode extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String codeGroup;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private int sortOrder;

    public static CommonCode of(String codeGroup, String code, String name, boolean active, int sortOrder) {
        CommonCode cc = new CommonCode();
        cc.codeGroup = codeGroup;
        cc.code = code;
        cc.name = name;
        cc.active = active;
        cc.sortOrder = sortOrder;
        return cc;
    }

    public void change(String name, boolean active, int sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name.trim();
        this.active = active;
        this.sortOrder = sortOrder;
    }
}

