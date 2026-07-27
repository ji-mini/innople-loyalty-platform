package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.domain.member.MemberCredential;

import java.util.Optional;
import java.util.UUID;

public interface MemberCredentialService {
    CredentialInfo provision(UUID memberId, String phoneNumber, String email, String rawPassword);

    CredentialInfo syncProfile(UUID memberId, String phoneNumber, String email);

    void disable(UUID memberId);

    /**
     * 최종 탈회 시 자격증명(로그인 ID/비밀번호 해시)을 익명화하고 soft-delete 한다.
     * 자격증명이 없으면 no-op.
     */
    void anonymizeAndDisable(UUID memberId, String loginIdToken);

    Optional<MemberCredential> findByPhoneNumber(String phoneNumber);

    Optional<MemberCredential> findActiveByPhoneNumber(String phoneNumber);

    Optional<MemberCredential> findByMemberId(UUID memberId);

    boolean isAppLoginEnabled(UUID memberId);

    String getLoginId(UUID memberId);

    record CredentialInfo(
            boolean appLoginEnabled,
            String loginId
    ) {
    }
}
