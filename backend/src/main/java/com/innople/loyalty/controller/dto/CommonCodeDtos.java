package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public final class CommonCodeDtos {
    private CommonCodeDtos() {
    }

    public record CreateRequest(
            @NotBlank String codeGroup,
            @NotBlank String code,
            @NotBlank String name,
            boolean active,
            int sortOrder
    ) {
    }

    public record UpdateRequest(
            @NotBlank String name,
            boolean active,
            int sortOrder
    ) {
    }

    public record CommonCodeResponse(
            UUID id,
            String codeGroup,
            String code,
            String name,
            boolean active,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

