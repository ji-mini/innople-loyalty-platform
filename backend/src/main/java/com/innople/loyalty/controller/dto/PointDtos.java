package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.points.PointEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class PointDtos {

    /** 포인트 이력 조회용 (API 응답). */
    public record PointLedgerResponse(
            UUID id,
            String memberNo,
            PointEventType eventType,
            long amount,
            String reason,
            String sourceChannel,
            Instant expiresAt,
            String approvalNo,
            String referenceType,
            String referenceId,
            Instant createdAt
    ) {
    }

    private PointDtos() {
    }

    public record EarnRequest(
            @NotNull UUID memberId,
            @Positive long amount,
            @NotNull Instant expiresAt,
            String reason,
            @Size(max = 12) @Pattern(regexp = "^[A-Za-z0-9]{1,12}$") String approvalNo,
            @Size(max = 50) String referenceType,
            @Size(max = 100) String referenceId
    ) {
    }

    public record UseRequest(
            @NotNull UUID memberId,
            @Positive long amount,
            String reason,
            @Size(max = 12) @Pattern(regexp = "^[A-Za-z0-9]{1,12}$") String approvalNo,
            @Size(max = 50) String referenceType,
            @Size(max = 100) String referenceId
    ) {
    }

    public record ManualExpireRequest(
            @NotNull UUID memberId,
            Instant referenceAt,
            String reason,
            @Size(max = 12) @Pattern(regexp = "^[A-Za-z0-9]{1,12}$") String approvalNo,
            @Size(max = 50) String referenceType,
            @Size(max = 100) String referenceId
    ) {
    }

    public record PointOperationResponse(
            UUID ledgerId,
            String approvalNo,
            PointEventType eventType,
            long amount,
            long currentBalance,
            Instant occurredAt
    ) {
    }

    public record ExpireBatchRunRequest(
            Instant referenceAt
    ) {
    }

    public record ExpireBatchRunResponse(
            Instant referenceAt,
            int tenantCount,
            int processedMemberCount,
            long expiredPointAmount
    ) {
    }

    public record ReconcileBalanceRequest(
            UUID memberId
    ) {
    }

    public record ReconcileBalanceResponse(
            UUID memberId,
            long ledgerBalance,
            long previousBalance,
            long currentBalance,
            boolean corrected
    ) {
    }

    public record ReconcileTenantBalancesResponse(
            int processedAccountCount,
            int correctedAccountCount
    ) {
    }
}
