package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.MemberCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberCredentialRepository extends JpaRepository<MemberCredential, UUID> {
    boolean existsByTenantIdAndPhoneNumberAndDeletedFalse(UUID tenantId, String phoneNumber);

    Optional<MemberCredential> findByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);

    Optional<MemberCredential> findByTenantIdAndPhoneNumberAndDeletedFalse(UUID tenantId, String phoneNumber);

    Optional<MemberCredential> findByTenantIdAndMemberId(UUID tenantId, UUID memberId);
}
