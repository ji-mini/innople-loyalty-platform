package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.points.PointEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class PosPointDtos {

    private PosPointDtos() {
    }

    public record PosEarnRequest(
            UUID memberId,
            @Size(max = 30) String memberNo,
            @NotBlank @Size(max = 30) String storeCode,
            @NotBlank @Size(max = 30) String posNo,
            @NotBlank @Size(max = 40) String transactionNo,
            @Positive long paymentAmount,
            @PositiveOrZero Long totalPaymentAmount,
            @PositiveOrZero Long discountAmount,
            @Size(max = 500) String reason
    ) {
    }

    public record PosUseRequest(
            UUID memberId,
            @Size(max = 30) String memberNo,
            @NotBlank @Size(max = 30) String storeCode,
            @NotBlank @Size(max = 30) String posNo,
            @NotBlank @Size(max = 40) String transactionNo,
            @Positive long amount,
            @PositiveOrZero Long paymentAmount,
            @Size(max = 500) String reason
    ) {
    }

    public record PosPointOperationResponse(
            UUID ledgerId,
            String approvalNo,
            PointEventType eventType,
            long amount,
            long currentBalance,
            Instant occurredAt,
            boolean duplicated
    ) {
    }
}
