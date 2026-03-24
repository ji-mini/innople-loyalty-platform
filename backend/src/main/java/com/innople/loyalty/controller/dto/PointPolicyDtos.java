package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public final class PointPolicyDtos {
    private PointPolicyDtos() {
    }

    public record CreateRequest(
            @NotBlank String pointType,
            @NotBlank String name,
            @Min(1) int validityDays,
            boolean enabled,
            String description
    ) {
    }

    public record UpdateRequest(
            @NotBlank String pointType,
            @NotBlank String name,
            @Min(1) int validityDays,
            boolean enabled,
            String description
    ) {
    }

    public record PointPolicyResponse(
            UUID id,
            String pointType,
            String name,
            int validityDays,
            boolean enabled,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

