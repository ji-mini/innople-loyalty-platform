package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.member.MemberCredential;
import com.innople.loyalty.repository.MemberCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberCredentialServiceImpl implements MemberCredentialService {

    private final MemberCredentialRepository memberCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CredentialInfo provision(UUID memberId, String phoneNumber, String email, String rawPassword) {
        UUID tenantId = TenantContext.requireTenantId();
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        String normalizedEmail = normalizeEmailOrNull(email);
        String passwordHash = encodePassword(rawPassword);

        MemberCredential activeByPhone = memberCredentialRepository
                .findByTenantIdAndPhoneNumberAndDeletedFalse(tenantId, normalizedPhoneNumber)
                .orElse(null);
        if (activeByPhone != null && !activeByPhone.getMemberId().equals(memberId)) {
            throw new MemberAuthExceptions.MemberCredentialAlreadyExistsException("이미 앱 로그인에 사용 중인 휴대폰 번호입니다.");
        }

        MemberCredential credential = memberCredentialRepository.findByTenantIdAndMemberId(tenantId, memberId)
                .orElse(null);
        if (credential == null) {
            credential = MemberCredential.of(memberId, normalizedPhoneNumber, normalizedEmail, passwordHash);
        } else {
            credential.enable(normalizedPhoneNumber, normalizedEmail, passwordHash);
        }
        MemberCredential savedCredential = memberCredentialRepository.save(credential);
        return new CredentialInfo(!savedCredential.isDeleted(), savedCredential.getPhoneNumber());
    }

    @Override
    @Transactional
    public CredentialInfo syncProfile(UUID memberId, String phoneNumber, String email) {
        UUID tenantId = TenantContext.requireTenantId();
        MemberCredential credential = memberCredentialRepository.findByTenantIdAndMemberId(tenantId, memberId)
                .orElse(null);
        if (credential == null) {
            return new CredentialInfo(false, null);
        }

        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        String normalizedEmail = normalizeEmailOrNull(email);
        if (!credential.isDeleted()) {
            MemberCredential activeByPhone = memberCredentialRepository
                    .findByTenantIdAndPhoneNumberAndDeletedFalse(tenantId, normalizedPhoneNumber)
                    .orElse(null);
            if (activeByPhone != null && !activeByPhone.getMemberId().equals(memberId)) {
                throw new MemberAuthExceptions.MemberCredentialAlreadyExistsException("이미 앱 로그인에 사용 중인 휴대폰 번호입니다.");
            }
        }

        credential.updateProfile(normalizedPhoneNumber, normalizedEmail);
        MemberCredential savedCredential = memberCredentialRepository.save(credential);
        return new CredentialInfo(!savedCredential.isDeleted(), savedCredential.getPhoneNumber());
    }

    @Override
    @Transactional
    public void disable(UUID memberId) {
        UUID tenantId = TenantContext.requireTenantId();
        memberCredentialRepository.findByTenantIdAndMemberId(tenantId, memberId)
                .ifPresent(credential -> {
                    credential.disable();
                    memberCredentialRepository.save(credential);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberCredential> findByPhoneNumber(String phoneNumber) {
        UUID tenantId = TenantContext.requireTenantId();
        return memberCredentialRepository.findByTenantIdAndPhoneNumber(tenantId, normalizePhoneNumber(phoneNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberCredential> findActiveByPhoneNumber(String phoneNumber) {
        UUID tenantId = TenantContext.requireTenantId();
        return memberCredentialRepository.findByTenantIdAndPhoneNumberAndDeletedFalse(tenantId, normalizePhoneNumber(phoneNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MemberCredential> findByMemberId(UUID memberId) {
        UUID tenantId = TenantContext.requireTenantId();
        return memberCredentialRepository.findByTenantIdAndMemberId(tenantId, memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAppLoginEnabled(UUID memberId) {
        return findByMemberId(memberId)
                .map(credential -> !credential.isDeleted())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getLoginId(UUID memberId) {
        return findByMemberId(memberId)
                .filter(credential -> !credential.isDeleted())
                .map(MemberCredential::getPhoneNumber)
                .orElse(null);
    }

    private String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("초기 비밀번호가 필요합니다.");
        }
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        return passwordEncoder.encode(rawPassword);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        String digits = phoneNumber.replaceAll("\\D", "");
        if (digits.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        return digits;
    }

    private String normalizeEmailOrNull(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
