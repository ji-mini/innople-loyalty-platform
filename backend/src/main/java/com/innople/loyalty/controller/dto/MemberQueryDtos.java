package com.innople.loyalty.controller.dto;

import com.innople.loyalty.domain.member.CalendarType;
import com.innople.loyalty.domain.member.Gender;
import com.innople.loyalty.domain.member.HistoryActorType;

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
            UUID gradeId,
            String gradeName,
            String phoneNumber,
            String email,
            String webId,
            Instant joinedAt,
            Instant dormantAt,
            Instant withdrawnAt,
            boolean appLoginEnabled
    ) {
    }

    public record MemberDetailResponse(
            UUID id,
            String memberNo,
            String name,
            long pointBalance,
            String gradeName,
            UUID gradeId,
            LocalDate birthDate,
            CalendarType calendarType,
            Gender gender,
            String phoneNumber,
            String email,
            MemberDtos.AddressResponse address,
            String webId,
            String statusCode,
            Instant joinedAt,
            Instant dormantAt,
            Instant suspendedAt,
            Instant withdrawRequestedAt,
            Instant withdrawnAt,
            String ci,
            String anniversaries,
            boolean appLoginEnabled,
            String appLoginId
    ) {
    }

    public record MeResponse(
            UUID id,
            String memberNo,
            String name,
            String email,
            String phone,
            String gradeName,
            long pointBalance
    ) {
    }

    public record MemberLedgerResponse(
            UUID id,
            String eventType,
            String statusCodeBefore,
            String statusCodeAfter,
            Instant createdAt
    ) {
    }

    public record MemberLoginHistoryResponse(
            UUID id,
            String loginId,
            String deviceName,
            String osName,
            String ip,
            String userAgent,
            Instant createdAt
    ) {
    }

    /**
     * 회원 상태 변경 이력 조회 응답.
     * <p>actorType 은 enum.name() 문자열로 노출한다. changedByName 은 관리자가 삭제/부재(SYSTEM·MEMBER 주체)일 때
     * null 이 되는 것을 정상 케이스로 취급한다. fromStatus/toStatus 는 원시 코드 문자열이며 라벨 변환하지 않는다.
     */
    public record MemberStatusHistoryResponse(
            UUID id,
            String actorType,
            UUID changedBy,
            String changedByName,
            String reason,
            Instant changedAt,
            String fromStatus,
            String toStatus
    ) {
        /** JPQL 생성자 표현식에서 {@code h.actorType}(enum)을 그대로 받아 name() 문자열로 변환한다. */
        public MemberStatusHistoryResponse(
                UUID id,
                HistoryActorType actorType,
                UUID changedBy,
                String changedByName,
                String reason,
                Instant changedAt,
                String fromStatus,
                String toStatus
        ) {
            this(id, actorType != null ? actorType.name() : null, changedBy, changedByName,
                    reason, changedAt, fromStatus, toStatus);
        }
    }

    /**
     * 회원 등급 변경 이력 조회 응답.
     * <p>actorType 은 enum.name() 문자열로 노출한다. changedByName 은 관리자가 삭제/부재(SYSTEM 주체)일 때 null 이 될 수 있다.
     * fromGradeId/fromGradeName 은 최초 등급 부여 시 null 일 수 있다.
     */
    public record MemberGradeHistoryResponse(
            UUID id,
            String actorType,
            UUID changedBy,
            String changedByName,
            String reason,
            Instant changedAt,
            UUID fromGradeId,
            String fromGradeName,
            UUID toGradeId,
            String toGradeName
    ) {
        /** JPQL 생성자 표현식에서 {@code h.actorType}(enum)을 그대로 받아 name() 문자열로 변환한다. */
        public MemberGradeHistoryResponse(
                UUID id,
                HistoryActorType actorType,
                UUID changedBy,
                String changedByName,
                String reason,
                Instant changedAt,
                UUID fromGradeId,
                String fromGradeName,
                UUID toGradeId,
                String toGradeName
        ) {
            this(id, actorType != null ? actorType.name() : null, changedBy, changedByName,
                    reason, changedAt, fromGradeId, fromGradeName, toGradeId, toGradeName);
        }
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
