package com.innople.loyalty.service.member;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberDuplicationServiceImpl implements MemberDuplicationService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public DuplicationResult check(String memberNo, String phoneNumber, String webId) {
        UUID tenantId = TenantContext.requireTenantId();

        boolean memberNoDup = memberNo != null && !memberNo.isBlank()
                && memberRepository.existsByTenantIdAndMemberNo(tenantId, memberNo.trim());

        String normalizedPhone = normalizePhoneNumberOrNull(phoneNumber);
        boolean phoneDup = normalizedPhone != null
                && memberRepository.existsByTenantIdAndPhoneNumber(tenantId, normalizedPhone);

        String normalizedWebId = normalizeWebIdOrNull(webId);
        boolean webIdDup = normalizedWebId != null
                && memberRepository.existsByTenantIdAndWebId(tenantId, normalizedWebId);

        return new DuplicationResult(memberNoDup, phoneDup, webIdDup);
    }

    private String normalizePhoneNumberOrNull(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    private String normalizeWebIdOrNull(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        return v.isEmpty() ? null : v;
    }
}

