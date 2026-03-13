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
        name = "addresses",
        indexes = {
                @Index(name = "idx_addresses_tenant_id", columnList = "tenantId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Column(nullable = false, length = 10)
    private String zipCode;

    @Column(nullable = false, length = 255)
    private String roadAddress;

    @Column(nullable = true, length = 255)
    private String jibunAddress;

    @Column(nullable = true, length = 255)
    private String detailAddress;

    @Column(nullable = true, length = 100)
    private String buildingName;

    @Column(nullable = true, length = 50)
    private String siDo;

    @Column(nullable = true, length = 50)
    private String siGunGu;

    @Column(nullable = true, length = 50)
    private String eupMyeonDong;

    @Column(nullable = true, length = 20)
    private String legalDongCode;

    public static Address of(
            String zipCode,
            String roadAddress,
            String jibunAddress,
            String detailAddress,
            String buildingName,
            String siDo,
            String siGunGu,
            String eupMyeonDong,
            String legalDongCode
    ) {
        Address address = new Address();
        address.zipCode = requireNonBlank(zipCode, "zipCode");
        address.roadAddress = requireNonBlank(roadAddress, "roadAddress");
        address.jibunAddress = blankToNull(jibunAddress);
        address.detailAddress = blankToNull(detailAddress);
        address.buildingName = blankToNull(buildingName);
        address.siDo = blankToNull(siDo);
        address.siGunGu = blankToNull(siGunGu);
        address.eupMyeonDong = blankToNull(eupMyeonDong);
        address.legalDongCode = blankToNull(legalDongCode);
        return address;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
