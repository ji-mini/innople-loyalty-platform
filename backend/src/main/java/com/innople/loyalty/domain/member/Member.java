package com.innople.loyalty.domain.member;

import com.innople.loyalty.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "members",
        indexes = {
                @Index(name = "idx_members_tenant_id", columnList = "tenantId"),
                @Index(name = "uk_members_tenant_member_no", columnList = "tenantId,memberNo", unique = true),
                @Index(name = "uk_members_tenant_web_id", columnList = "tenantId,webId", unique = true),
                @Index(name = "uk_members_tenant_ci", columnList = "tenantId,ci", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String memberNo;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = true)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private CalendarType calendarType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 10)
    private Gender gender;

    @Column(nullable = true, length = 30)
    private String phoneNumber;

    @Column(nullable = true, length = 500)
    private String address;

    @Column(nullable = true, length = 100)
    private String webId;

    /**
     * Member status is managed by common code (group: MEMBER_STATUS).
     */
    @Column(nullable = false, length = 50)
    private String statusCode;

    @Column(nullable = false)
    private LocalDate joinedAt;

    @Column(nullable = true)
    private LocalDate dormantAt;

    @Column(nullable = true)
    private LocalDate withdrawnAt;

    @Column(nullable = true, length = 200)
    private String ci;

    @Column(nullable = true, length = 1000)
    private String anniversaries;

    public static Member register(
            String memberNo,
            String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            String phoneNumber,
            String address,
            String webId,
            String statusCode,
            LocalDate joinedAt,
            LocalDate dormantAt,
            LocalDate withdrawnAt,
            String ci,
            String anniversaries
    ) {
        Member member = new Member();
        member.memberNo = requireText(memberNo, "memberNo");
        member.name = requireText(name, "name");
        member.birthDate = birthDate;
        member.calendarType = calendarType;
        member.gender = (gender != null) ? gender : Gender.UNKNOWN;
        member.phoneNumber = phoneNumber;
        member.address = address;
        member.webId = webId;
        member.statusCode = requireText(statusCode, "statusCode");
        member.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt must not be null");
        member.dormantAt = dormantAt;
        member.withdrawnAt = withdrawnAt;
        member.ci = ci;
        member.anniversaries = anniversaries;
        return member;
    }

    public void updateInfo(
            String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            String phoneNumber,
            String address,
            String webId,
            String ci,
            String anniversaries
    ) {
        this.name = requireText(name, "name");
        this.birthDate = birthDate;
        this.calendarType = calendarType;
        this.gender = (gender != null) ? gender : Gender.UNKNOWN;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.webId = webId;
        this.ci = ci;
        this.anniversaries = anniversaries;
    }

    public void updateStatus(String statusCode, LocalDate dormantAt, LocalDate withdrawnAt) {
        this.statusCode = requireText(statusCode, "statusCode");
        this.dormantAt = dormantAt;
        this.withdrawnAt = withdrawnAt;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}

