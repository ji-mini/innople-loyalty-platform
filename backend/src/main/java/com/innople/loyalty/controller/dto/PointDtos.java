package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.points.PointEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.UUID;

public final class PointDtos {
    private PointDtos() {
    }

    public record EarnRequest(
            @NotNull UUID memberId,
            @Positive long amount,
            @NotNull Instant expiresAt,
            String reason
    ) {
    }

    public record UseRequest(
            @NotNull UUID memberId,
            @Positive long amount,
            String reason
    ) {
    }

    public record ManualExpireRequest(
            @NotNull UUID memberId,
            Instant referenceAt,
            String reason
    ) {
    }

    public record PointOperationResponse(
            UUID ledgerId,
            PointEventType eventType,
            long amount,
            long currentBalance,
            Instant occurredAt
    ) {
    }
}

