package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.member.CalendarType;
import com.innople.loyalty.domain.member.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public final class MemberDtos {
    private MemberDtos() {
    }

    public record AddressRequest(
            @NotBlank @Size(max = 10) String zipCode,
            @NotBlank @Size(max = 255) String roadAddress,
            @Size(max = 255) String jibunAddress,
            @Size(max = 255) String detailAddress,
            @Size(max = 100) String buildingName,
            @Size(max = 50) String siDo,
            @Size(max = 50) String siGunGu,
            @Size(max = 50) String eupMyeonDong,
            @Size(max = 20) String legalDongCode
    ) {
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 50) String memberNo,
            @NotBlank @Size(max = 100) String name,
            @NotNull LocalDate birthDate,
            CalendarType calendarType,
            @NotNull Gender gender,
            @Size(max = 30) String phoneNumber,
            @NotBlank @Email @Size(max = 255) String email,
            @Valid AddressRequest address,
            @Size(max = 100) String webId,
            @Size(max = 50) String statusCode,
            LocalDate joinedAt,
            @Size(max = 200) String ci,
            @Size(max = 1000) String anniversaries,
            Boolean appLoginAllowed,
            @Size(min = 8, max = 100) String initialPassword,
            Boolean autoGeneratePassword,
            Boolean sendInitialPasswordLink,
            // 인증 상태 placeholder. 실제 인증 인프라(SMS/이메일) 연동 전까지는 클라이언트가 보내지 않을 수 있으며,
            // verification-required=false(개발)에서는 값이 없어도 등록이 정상 동작한다.
            Boolean phoneVerified,
            Boolean emailVerified
    ) {
    }

    public record UpdateInfoRequest(
            @NotBlank @Size(max = 100) String name,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            @Size(max = 30) String phoneNumber,
            @Size(max = 255) String email,
            @Valid AddressRequest address,
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

    public record UpdateAppLoginRequest(
            @NotNull Boolean enabled,
            @Size(min = 8, max = 100) String initialPassword,
            Boolean autoGeneratePassword
    ) {
    }

    public record AddressResponse(
            UUID id,
            String zipCode,
            String roadAddress,
            String jibunAddress,
            String detailAddress,
            String buildingName,
            String siDo,
            String siGunGu,
            String eupMyeonDong,
            String legalDongCode
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
            String email,
            AddressResponse address,
            String webId,
            String statusCode,
            LocalDate joinedAt,
            LocalDate dormantAt,
            LocalDate withdrawnAt,
            String ci,
            String anniversaries,
            boolean appLoginEnabled,
            String appLoginId,
            String generatedPassword
    ) {
    }

    public record AppLoginResponse(
            String memberNo,
            boolean appLoginEnabled,
            String appLoginId,
            String generatedPassword
    ) {
    }

    public record SuggestMemberNoResponse(
            String memberNo
    ) {
    }

    public record DuplicationCheckResponse(
            boolean memberNoDuplicated,
            boolean phoneNumberDuplicated,
            boolean webIdDuplicated
    ) {
    }
}

