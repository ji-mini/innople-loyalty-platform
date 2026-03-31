package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.stamp.StampEventType;
import com.innople.loyalty.domain.stamp.StampIssuanceMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class StampDtos {

    public record CouponTemplateResponse(
            UUID id,
            String name,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CouponTemplateCreateRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 2000) String description,
            boolean active
    ) {
    }

    public record CouponTemplateUpdateRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 2000) String description,
            boolean active
    ) {
    }

    public record StampPolicyResponse(
            UUID id,
            String name,
            long amountWonPerStamp,
            int stampsRequiredForCoupon,
            UUID couponTemplateId,
            String couponTemplateName,
            StampIssuanceMode issuanceMode,
            boolean active,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record StampPolicyCreateRequest(
            @NotBlank @Size(max = 200) String name,
            @Positive long amountWonPerStamp,
            @Positive int stampsRequiredForCoupon,
            @NotNull UUID couponTemplateId,
            @NotNull StampIssuanceMode issuanceMode,
            boolean active
    ) {
    }

    public record StampPolicyUpdateRequest(
            @NotBlank @Size(max = 200) String name,
            @Positive long amountWonPerStamp,
            @Positive int stampsRequiredForCoupon,
            @NotNull UUID couponTemplateId,
            @NotNull StampIssuanceMode issuanceMode,
            boolean active
    ) {
    }

    public record StampLedgerRow(
            UUID id,
            String memberNo,
            StampEventType eventType,
            int stampDelta,
            String reason,
            String referenceType,
            String referenceId,
            Long purchaseAmountWon,
            Instant createdAt
    ) {
    }

    public record StampManualGrantRequest(
            @NotNull UUID memberId,
            @Positive int stamps,
            @NotBlank @Size(max = 500) String reason
    ) {
    }

    public record StampManualGrantResponse(UUID ledgerId, int currentBalance) {
    }

    public record StampPosEarnRequest(
            @NotNull UUID memberId,
            @Positive long purchaseAmountWon,
            @NotBlank @Size(max = 100) String posOrderReferenceId
    ) {
    }

    public record StampPosEarnResponse(
            UUID ledgerId,
            int stampsEarned,
            int currentBalance,
            boolean idempotentReplay
    ) {
    }

    public record StampClaimRequest(@NotNull UUID memberId) {
    }

    public record StampClaimResponse(UUID ledgerId, UUID couponIssueId, int currentBalance) {
    }

    public record StampAutoRedeemBatchResponse(int accountsProcessed, int couponsIssued) {
    }

    private StampDtos() {
    }
}
