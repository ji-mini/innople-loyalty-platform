package com.innople.loyalty.service.points;

import com.innople.loyalty.domain.points.PointEventType;

import java.time.Instant;
import java.util.UUID;

public record PointOperationResult(
        UUID ledgerId,
        String approvalNo,
        PointEventType eventType,
        long amount,
        long currentBalance,
        Instant occurredAt
) {
}

