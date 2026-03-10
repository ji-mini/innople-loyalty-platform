package com.innople.loyalty.service.admin;

import java.util.UUID;

public record AdminRegisterResult(
        UUID adminUserId,
        String phoneNumber,
        String email,
        String name
) {
}

