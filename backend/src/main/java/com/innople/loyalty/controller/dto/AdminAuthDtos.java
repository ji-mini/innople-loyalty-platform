package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.user.AdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public final class AdminAuthDtos {
    private AdminAuthDtos() {
    }

    public static final String PHONE_INPUT_REGEX = "^[0-9+\\-()\\s]{9,30}$";

    public record LoginRequest(
            @NotBlank @Pattern(regexp = PHONE_INPUT_REGEX) String phoneNumber,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            UUID adminUserId,
            String phoneNumber,
            String email,
            String name,
            AdminRole role,
            String accessToken
    ) {
    }

    public record RegisterRequest(
            @NotBlank @Pattern(regexp = PHONE_INPUT_REGEX) String phoneNumber,
            @Email String email,
            @NotBlank String name,
            @NotBlank String password
    ) {
    }

    public record RegisterResponse(
            UUID adminUserId,
            String phoneNumber,
            String email,
            String name,
            AdminRole role
    ) {
    }
}

