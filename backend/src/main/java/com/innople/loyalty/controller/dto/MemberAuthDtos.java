package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class MemberAuthDtos {
    private MemberAuthDtos() {
    }

    public static final String PHONE_INPUT_REGEX = "^[0-9+\\-()\\s]{9,30}$";

    public record SignupRequest(
            @NotBlank String tenantId,
            @NotBlank String name,
            String email,
            @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
            @NotBlank @Pattern(regexp = PHONE_INPUT_REGEX, message = "phoneNumber format is invalid") String phoneNumber,
            @Pattern(regexp = PHONE_INPUT_REGEX, message = "phone format is invalid") String phone
    ) {
    }

    public record LoginRequest(
            @NotBlank String tenantId,
            @NotBlank @Pattern(regexp = PHONE_INPUT_REGEX, message = "phoneNumber format is invalid") String phoneNumber,
            @NotBlank String password
    ) {
    }

    public record AuthResponse(
            String accessToken,
            MemberInfo member
    ) {
    }

    public record MemberInfo(
            UUID memberId,
            String memberNo,
            String name,
            String email,
            String phone,
            String grade,
            long pointBalance
    ) {
    }
}
