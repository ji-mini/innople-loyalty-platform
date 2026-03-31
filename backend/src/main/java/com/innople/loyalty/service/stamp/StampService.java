package com.innople.loyalty.service.stamp;

import java.util.UUID;

public interface StampService {

    PosEarnResult earnFromPos(UUID memberId, long purchaseAmountWon, String posOrderReferenceId);

    ManualGrantResult grantManual(UUID memberId, int stamps, String reason);

    ClaimResult claimCouponForMember(UUID memberId);

    /** AUTO 정책: 현재 테넌트에서 조건 충족 계정에 대해 자동 전환(배치/수동 트리거). */
    AutoRedeemBatchResult runAutoRedeemForCurrentTenant();

    record PosEarnResult(
            UUID ledgerId,
            int stampsEarned,
            int currentBalance,
            boolean idempotentReplay
    ) {
    }

    record ManualGrantResult(UUID ledgerId, int currentBalance) {
    }

    record ClaimResult(UUID ledgerId, UUID couponIssueId, int currentBalance) {
    }

    record AutoRedeemBatchResult(int accountsProcessed, int couponsIssued) {
    }
}
