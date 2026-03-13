package com.innople.loyalty.service.member;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberNumberServiceImpl implements MemberNumberService {

    private final TenantRepository tenantRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public SuggestedMemberNo suggestForPhoneNumber(String phoneNumber) {
        UUID tenantId = TenantContext.requireTenantId();

        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("tenant not found"));

        String rep = normalizeRepCode(tenant.getRepresentativeCode());
        String last4 = last4Digits(phoneNumber);

        String prefix = rep + last4;
        String maxMemberNo = memberRepository.findMaxMemberNoByTenantIdAndPrefix(tenantId, prefix);

        int next = 1;
        if (maxMemberNo != null && maxMemberNo.length() >= prefix.length() + 4) {
            String suffix = maxMemberNo.substring(prefix.length());
            if (suffix.length() >= 4) {
                suffix = suffix.substring(0, 4);
            }
            try {
                next = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException ignored) {
                next = 1;
            }
        }
        if (next > 9999) {
            throw new IllegalStateException("memberNo sequence overflow (0001-9999)");
        }
        String memberNo = prefix + String.format(Locale.ROOT, "%04d", next);
        return new SuggestedMemberNo(memberNo);
    }

    private String normalizeRepCode(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("tenant representativeCode is missing");
        }
        String v = raw.trim().toUpperCase(Locale.ROOT);
        if (v.length() != 2) {
            throw new IllegalStateException("tenant representativeCode must be 2 letters");
        }
        return v;
    }

    private String last4Digits(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        String digits = rawPhoneNumber.replaceAll("\\D", "");
        if (digits.length() < 4) {
            throw new IllegalArgumentException("phoneNumber must have at least 4 digits");
        }
        return digits.substring(digits.length() - 4);
    }
}
