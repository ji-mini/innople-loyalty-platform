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

@Getter
@Entity
@Table(
        name = "member_login_histories",
        indexes = {
                @Index(name = "idx_member_login_histories_tenant_created_at", columnList = "tenantId,createdAt"),
                @Index(name = "idx_member_login_histories_tenant_member_created_at", columnList = "tenantId,memberId,createdAt")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberLoginHistory extends BaseEntity {

    @Column(nullable = false)
    private UUID memberId;

    @Column(nullable = false, length = 50)
    private String memberNo;

    @Column(nullable = false, length = 50)
    private String loginId;

    @Column(length = 80)
    private String deviceName;

    @Column(length = 80)
    private String osName;

    @Column(length = 45)
    private String ip;

    @Column(length = 400)
    private String userAgent;

    public static MemberLoginHistory of(
            UUID memberId,
            String memberNo,
            String loginId,
            String deviceName,
            String osName,
            String ip,
            String userAgent
    ) {
        MemberLoginHistory history = new MemberLoginHistory();
        history.memberId = memberId;
        history.memberNo = memberNo;
        history.loginId = loginId;
        history.deviceName = deviceName;
        history.osName = osName;
        history.ip = ip;
        history.userAgent = userAgent;
        return history;
    }
}
