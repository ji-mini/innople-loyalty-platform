package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.domain.member.Member;

public interface MemberLoginHistoryService {
    void recordSuccess(Member member, String loginId, String deviceName, String osName, String ip, String userAgent);
}
