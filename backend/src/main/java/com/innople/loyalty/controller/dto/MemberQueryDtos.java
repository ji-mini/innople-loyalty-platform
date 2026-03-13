package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.member.CalendarType;
import com.innople.loyalty.domain.member.Gender;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class MemberQueryDtos {
    private MemberQueryDtos() {
    }

    public record MemberSummaryResponse(
            UUID id,
            String memberNo,
            String name,
            long pointBalance,
            String statusCode,
            String phoneNumber,
            String email,
            String webId,
            LocalDate joinedAt,
            LocalDate dormantAt,
            LocalDate withdrawnAt
    ) {
    }

    public record MemberDetailResponse(
            UUID id,
            String memberNo,
            String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            String phoneNumber,
            String email,
            MemberDtos.AddressResponse address,
            String webId,
            String statusCode,
            LocalDate joinedAt,
            LocalDate dormantAt,
            LocalDate withdrawnAt,
            String ci,
            String anniversaries
    ) {
    }

    public record MemberLedgerResponse(
            UUID id,
            String memberNo,
            String eventType,
            String statusCodeBefore,
            String statusCodeAfter,
            Instant createdAt
    ) {
    }

    public record PagedResponse<T>(
            List<T> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
