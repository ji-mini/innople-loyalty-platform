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

    PointOperationResult earnFromPurchase(UUID memberId, long purchaseAmount, Long totalPurchaseAmount, Long discountAmount,
                                          Instant expiresAt, String reason, String approvalNo,
                                          String referenceType, String referenceId, String sourceChannel);

    PointOperationResult use(UUID memberId, long amount, String reason, String approvalNo,
                             String referenceType, String referenceId, String sourceChannel);

    PointOperationResult manualExpire(UUID memberId, Instant referenceAt, String reason, String approvalNo,
                                      String referenceType, String referenceId);

    PointOperationResult autoExpire(UUID memberId, Instant referenceAt, String reason, String referenceType, String referenceId);

    /**
     * 회원 최종 탈회(WITHDRAWN) 시 잔여 포인트를 전량 소각한다.
     *
     * <p>만료(expire) 계열과 달리 만료일 도래 여부와 무관하게 잔량이 남은 모든 lot 을 대상으로 한다.
     * 상태 가드(assertPointOperationAllowed)를 거치지 않으며, PointAccount 가 없거나 소각 대상 잔량이 0이면
     * 예외 없이 no-op 로 통과한다(ledgerId/approvalNo=null, amount=0).</p>
     */
    PointOperationResult burnAll(UUID memberId, Instant referenceAt, String reason, String sourceChannel);
}

