package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public final class AdminAuthDtos {
    private AdminAuthDtos() {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            UUID adminUserId,
            String email,
            String name,
            String accessToken
    ) {
    }
}

