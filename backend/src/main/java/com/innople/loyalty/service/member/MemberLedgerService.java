package com.innople.loyalty.service.member;

import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedgerEventType;

public interface MemberLedgerService {
    void record(Member member, MemberLedgerEventType eventType, String statusCodeBefore, String statusCodeAfter);
}
