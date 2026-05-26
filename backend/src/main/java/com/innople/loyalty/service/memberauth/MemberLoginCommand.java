package com.innople.loyalty.service.memberauth;

public record MemberLoginCommand(
        String phoneNumber,
        String password
) {
}
