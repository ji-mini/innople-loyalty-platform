package com.innople.loyalty.service.member;

import com.innople.loyalty.domain.member.CalendarType;
import com.innople.loyalty.domain.member.Gender;

import java.time.LocalDate;
import java.util.UUID;

public record MemberResult(
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

