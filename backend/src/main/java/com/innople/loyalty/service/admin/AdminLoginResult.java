package com.innople.loyalty.service.admin;

import java.util.UUID;

public record AdminLoginResult(
        UUID adminUserId,
        String phoneNumber,
        String email,
        String name,
        String accessToken
) {
}

