package com.innople.loyalty.service.member;

import com.innople.loyalty.controller.dto.MemberDtos.AddressRequest;
import com.innople.loyalty.domain.member.CalendarType;
import com.innople.loyalty.domain.member.Gender;

import java.time.LocalDate;

public interface MemberService {
    MemberResult register(RegisterCommand command);

    MemberResult updateInfo(String memberNo, UpdateInfoCommand command);

    MemberResult updateStatus(String memberNo, UpdateStatusCommand command);

    MemberResult withdraw(String memberNo, WithdrawCommand command);

    record RegisterCommand(
            String memberNo,
            String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            String phoneNumber,
            String email,
            AddressRequest address,
            String webId,
            String statusCode,
            LocalDate joinedAt,
            String ci,
            String anniversaries
    ) {
    }

    record UpdateInfoCommand(
            String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            String phoneNumber,
            String email,
            AddressRequest address,
            String webId,
            String ci,
            String anniversaries
    ) {
    }

    record UpdateStatusCommand(
            String statusCode,
            LocalDate dormantAt
    ) {
    }

    record WithdrawCommand(
            LocalDate withdrawnAt,
            String reason
    ) {
    }
}

