package com.innople.loyalty.service.admin;

import java.util.UUID;

public record AdminRegisterResult(
        UUID adminUserId,
        String email,
        String name
) {
}

