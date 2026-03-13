package com.innople.loyalty.service.member;

public interface MemberDuplicationService {
    DuplicationResult check(String memberNo, String phoneNumber, String webId);

    record DuplicationResult(
            boolean memberNoDuplicated,
            boolean phoneNumberDuplicated,
            boolean webIdDuplicated
    ) {
    }
}

