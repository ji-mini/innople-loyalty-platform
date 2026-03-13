package com.innople.loyalty.service.member;

public interface MemberNumberService {
    SuggestedMemberNo suggestForPhoneNumber(String phoneNumber);

    record SuggestedMemberNo(
            String memberNo
    ) {
    }
}

