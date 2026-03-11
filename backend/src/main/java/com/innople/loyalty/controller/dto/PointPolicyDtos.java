package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.points.PointPolicyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class PointPolicyDtos {
    private PointPolicyDtos() {
    }

    public record CreateRequest(
            @NotNull PointPolicyType pointType,
            @NotBlank String name,
            @Min(1) int validityDays,
            boolean enabled,
            String description
    ) {
    }

    public record UpdateRequest(
            @NotNull PointPolicyType pointType,
            @NotBlank String name,
            @Min(1) int validityDays,
            boolean enabled,
            String description
    ) {
    }

    public record PointPolicyResponse(
            UUID id,
            PointPolicyType pointType,
            String name,
            int validityDays,
            boolean enabled,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

