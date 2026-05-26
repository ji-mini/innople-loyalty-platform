package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLoginHistory;
import com.innople.loyalty.repository.MemberLoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberLoginHistoryServiceImpl implements MemberLoginHistoryService {
    private final MemberLoginHistoryRepository memberLoginHistoryRepository;

    @Override
    public void recordSuccess(
            Member member,
            String loginId,
            String deviceName,
            String osName,
            String ip,
            String userAgent
    ) {
        memberLoginHistoryRepository.save(MemberLoginHistory.of(
                member.getId(),
                member.getMemberNo(),
                normalize(loginId, 50),
                normalize(deviceName, 80),
                normalize(osName, 80),
                normalize(ip, 45),
                normalize(userAgent, 400)
        ));
    }

    private String normalize(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
