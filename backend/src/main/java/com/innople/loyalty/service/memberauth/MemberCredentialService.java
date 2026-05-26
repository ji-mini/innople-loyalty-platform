package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.domain.member.MemberCredential;

import java.util.Optional;
import java.util.UUID;

public interface MemberCredentialService {
    CredentialInfo provision(UUID memberId, String phoneNumber, String email, String rawPassword);

    CredentialInfo syncProfile(UUID memberId, String phoneNumber, String email);

    void disable(UUID memberId);

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
