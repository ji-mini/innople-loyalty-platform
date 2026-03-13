package com.innople.loyalty.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class MembershipGradeDtos {

    private MembershipGradeDtos() {
    }

    public record CreateRequest(
            @NotBlank(message = "등급명은 필수입니다")
            String name,
            @NotNull(message = "레벨은 필수입니다")
            Integer level,
            String description
    ) {
    }

    public record UpdateRequest(
            @NotBlank(message = "등급명은 필수입니다")
            String name,
            @NotNull(message = "레벨은 필수입니다")
            Integer level,
            String description
    ) {
    }
}
