package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class MembershipGradeDtos {

    private MembershipGradeDtos() {
    }

    public record CreateRequest(
            @NotBlank(message = "등급명은 필수입니다")
            String name,
            @NotNull(message = "레벨은 필수입니다")
            Integer level,
            String description,
            /** 적립 대상 금액 대비 적립률(%). POS 등에서 적립 대상 금액으로 포인트 계산 시 사용 */
            @NotNull(message = "적립률은 필수입니다")
            @DecimalMin(value = "0", message = "적립률은 0 이상이어야 합니다")
            @DecimalMax(value = "100", message = "적립률은 100 이하여야 합니다")
            BigDecimal earnRatePercent
    ) {
    }

    public record UpdateRequest(
            @NotBlank(message = "등급명은 필수입니다")
            String name,
            @NotNull(message = "레벨은 필수입니다")
            Integer level,
            String description,
            @NotNull(message = "적립률은 필수입니다")
            @DecimalMin(value = "0", message = "적립률은 0 이상이어야 합니다")
            @DecimalMax(value = "100", message = "적립률은 100 이하여야 합니다")
            BigDecimal earnRatePercent
    ) {
    }
}
