package com.innople.loyalty.service.memberauth;

import java.util.UUID;

public record MemberAuthResult(
        String accessToken,
        UUID memberId,
        String memberNo,
        String name,
        String email,
        String phone,
        String gradeName,
        long pointBalance
) {
}
