package com.innople.loyalty.service.admin;

import java.util.UUID;

public record AdminLoginResult(
        UUID adminUserId,
        String email,
        String name,
        String accessToken
) {
}

