package com.innople.loyalty.repository;

import java.time.Instant;
import java.util.UUID;

/**
 * {@link MemberRepository.MemberSummaryView} 의 구현체.
 *
 * <p>회원 목록 조회는 가입일 필터를 동적 predicate 로 조립하기 위해 {@link MemberRepositoryImpl} 에서
 * JPQL constructor expression 으로 이 클래스에 매핑한다. currentBalance/appLoginEnabled 는
 * coalesce/subquery 결과이므로 wrapper 타입으로 받고 getter 에서 primitive 로 노출한다.</p>
 */
public class MemberSummaryRow implements MemberRepository.MemberSummaryView {

    private final UUID id;
    private final String memberNo;
    private final String name;
    private final String statusCode;
    private final UUID gradeId;
    private final String gradeName;
    private final String phoneNumber;
    private final String email;
    private final String webId;
    private final Instant joinedAt;
    private final Instant dormantAt;
    private final Instant withdrawnAt;
    private final Long pointBalance;
    private final Boolean appLoginEnabled;

    public MemberSummaryRow(
            UUID id,
            String memberNo,
            String name,
            String statusCode,
            UUID gradeId,
            String gradeName,
            String phoneNumber,
            String email,
            String webId,
            Instant joinedAt,
            Instant dormantAt,
            Instant withdrawnAt,
            Long pointBalance,
            Boolean appLoginEnabled
    ) {
        this.id = id;
        this.memberNo = memberNo;
        this.name = name;
        this.statusCode = statusCode;
        this.gradeId = gradeId;
        this.gradeName = gradeName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.webId = webId;
        this.joinedAt = joinedAt;
        this.dormantAt = dormantAt;
        this.withdrawnAt = withdrawnAt;
        this.pointBalance = pointBalance;
        this.appLoginEnabled = appLoginEnabled;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getMemberNo() {
        return memberNo;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getStatusCode() {
        return statusCode;
    }

    @Override
    public UUID getGradeId() {
        return gradeId;
    }

    @Override
    public String getGradeName() {
        return gradeName;
    }

    @Override
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getWebId() {
        return webId;
    }

    @Override
    public Instant getJoinedAt() {
        return joinedAt;
    }

    @Override
    public Instant getDormantAt() {
        return dormantAt;
    }

    @Override
    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }

    @Override
    public long getPointBalance() {
        return pointBalance != null ? pointBalance : 0L;
    }

    @Override
    public boolean getAppLoginEnabled() {
        return appLoginEnabled != null && appLoginEnabled;
    }
}
