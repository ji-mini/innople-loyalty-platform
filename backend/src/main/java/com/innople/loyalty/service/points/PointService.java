package com.innople.loyalty.service.points;

import java.time.Instant;
import java.util.UUID;

public interface PointService {
    /**
     * @param purchaseAmount       적립 시 기준 적립 대상 금액(원). 없으면 null.
     * @param totalPurchaseAmount 총 구매금액(원). POS 연동 시 선택, 없으면 null.
     * @param discountAmount      할인금액(원). POS 연동 시 선택, 없으면 null.
     * @param sourceChannel       적립 경로(예: 관리자 수기, POS 구매).
     */
    PointOperationResult earn(UUID memberId, long amount, Instant expiresAt, String reason, String approvalNo,
                              String referenceType, String referenceId,
                              Long purchaseAmount, Long totalPurchaseAmount, Long discountAmount,
                              String sourceChannel);

    PointOperationResult use(UUID memberId, long amount, String reason, String approvalNo,
                             String referenceType, String referenceId);

    PointOperationResult manualExpire(UUID memberId, Instant referenceAt, String reason, String approvalNo,
                                      String referenceType, String referenceId);

    PointOperationResult autoExpire(UUID memberId, Instant referenceAt, String reason, String referenceType, String referenceId);
}

