package com.innople.loyalty.service.points;

import java.time.Instant;
import java.util.UUID;

public interface PointService {
    PointOperationResult earn(UUID memberId, long amount, Instant expiresAt, String reason);

    PointOperationResult use(UUID memberId, long amount, String reason);

    PointOperationResult manualExpire(UUID memberId, Instant referenceAt, String reason);
}

