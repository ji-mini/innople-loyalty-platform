package com.innople.loyalty.service.admin;

import com.innople.loyalty.domain.user.AdminRole;

import java.util.UUID;

public record AdminLoginResult(
        UUID adminUserId,
        String phoneNumber,
        String email,
        String name,
        AdminRole role,
        String accessToken
) {
}

