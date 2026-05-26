package com.innople.loyalty.service.memberauth;

public record MemberSignupCommand(
        String name,
        String email,
        String password,
        String phone
) {
}
