package com.innople.loyalty.service.points;

import java.time.Instant;
import java.util.UUID;

public interface PointService {
    PointOperationResult earn(UUID memberId, long amount, Instant expiresAt, String reason, String approvalNo,
                              String referenceType, String referenceId);

    PointOperationResult use(UUID memberId, long amount, String reason, String approvalNo,
                             String referenceType, String referenceId);

    PointOperationResult manualExpire(UUID memberId, Instant referenceAt, String reason, String approvalNo,
                                      String referenceType, String referenceId);

    PointOperationResult autoExpire(UUID memberId, Instant referenceAt, String reason, String referenceType, String referenceId);
}

