package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.member.CalendarType;
import com.innople.loyalty.domain.member.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public final class MemberDtos {
    private MemberDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 50) String memberNo,
            @NotBlank @Size(max = 100) String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            @Size(max = 30) String phoneNumber,
            @Size(max = 500) String address,
            @Size(max = 100) String webId,
            @Size(max = 50) String statusCode,
            LocalDate joinedAt,
            @Size(max = 200) String ci,
            @Size(max = 1000) String anniversaries
    ) {
    }

    public record UpdateInfoRequest(
            @NotBlank @Size(max = 100) String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            @Size(max = 30) String phoneNumber,
            @Size(max = 500) String address,
            @Size(max = 100) String webId,
            @Size(max = 200) String ci,
            @Size(max = 1000) String anniversaries
    ) {
    }

    public record UpdateStatusRequest(
            @NotBlank @Size(max = 50) String statusCode,
            LocalDate dormantAt
    ) {
    }

    public record WithdrawRequest(
            LocalDate withdrawnAt,
            @Size(max = 500) String reason
    ) {
    }

    public record MemberResponse(
            UUID id,
            String memberNo,
            String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            String phoneNumber,
            String address,
            String webId,
            String statusCode,
            LocalDate joinedAt,
            LocalDate dormantAt,
            LocalDate withdrawnAt,
            String ci,
            String anniversaries
    ) {
    }
}

