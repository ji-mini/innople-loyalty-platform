package com.innople.loyalty.service.member;

import com.innople.loyalty.config.MemberVerificationProperties;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberDtos.AddressRequest;
import com.innople.loyalty.controller.dto.MemberDtos.AddressResponse;
import com.innople.loyalty.domain.code.CommonCode;
import com.innople.loyalty.domain.member.Address;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.member.MemberStatusHistory;
import com.innople.loyalty.repository.AddressRepository;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.MemberStatusHistoryRepository;
import com.innople.loyalty.repository.MembershipGradeRepository;
import com.innople.loyalty.service.memberauth.MemberCredentialService;
import com.innople.loyalty.service.points.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.innople.loyalty.service.member.MemberExceptions.InvalidMemberStatusException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberAlreadyExistsException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberPhoneAlreadyExistsException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberNotFoundException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberVerificationRequiredException;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private static final String DEFAULT_MEMBERSHIP_GRADE_NAME = "기본등급";
    // V22__add_members_tenant_phone_unique_index 에서 추가한 (tenant_id, phone_number) 유니크 제약 이름.
    private static final String PHONE_UNIQUE_CONSTRAINT = "uk_members_tenant_phone_number";
    // 최종 탈회(WITHDRAWN) 시 잔여 포인트 전량 소각에 사용할 원장 채널/사유.
    private static final String WITHDRAW_BURN_SOURCE = "ADMIN_WEB_WITHDRAW_BURN";
    private static final String WITHDRAW_BURN_REASON = "회원 탈회에 따른 포인트 전량 소각";

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final MemberLedgerService memberLedgerService;
    private final MemberStatusHistoryRepository memberStatusHistoryRepository;
    private final MembershipGradeRepository membershipGradeRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final MemberCredentialService memberCredentialService;
    private final InitialPasswordLinkSender initialPasswordLinkSender;
    private final MemberVerificationProperties memberVerificationProperties;
    private final PointService pointService;
    private static final Pattern WEB_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    @Override
    @Transactional
    public MemberResult register(RegisterCommand command) {
        UUID tenantId = TenantContext.requireTenantId();

        // 운영 등 verification-required=true 환경에서만 서버측에서 인증 완료 여부를 강제한다.
        // (프론트 버튼 비활성화는 우회 가능하므로 반드시 서버에서 검증)
        verifyContactVerification(command);

        String statusCode = (command.statusCode() == null || command.statusCode().isBlank())
                ? MemberStatusCodes.ACTIVE
                : command.statusCode().trim();

        validateStatusCode(tenantId, statusCode);
        validateWebId(command.webId());

        if (memberRepository.existsByTenantIdAndMemberNo(tenantId, command.memberNo())) {
            throw new MemberAlreadyExistsException("memberNo already exists");
        }
        String normalizedPhone = normalizePhoneOrNull(command.phoneNumber());
        if (normalizedPhone != null && memberRepository.existsByTenantIdAndPhoneNumber(tenantId, normalizedPhone)) {
            throw new MemberPhoneAlreadyExistsException();
        }
        String normalizedWebId = normalizeWebIdOrNull(command.webId());
        if (normalizedWebId != null && memberRepository.existsByTenantIdAndWebId(tenantId, normalizedWebId)) {
            throw new MemberAlreadyExistsException("webId already exists");
        }
        boolean appLoginAllowed = Boolean.TRUE.equals(command.appLoginAllowed());
        if (appLoginAllowed && normalizedPhone == null) {
            throw new IllegalArgumentException("앱 로그인을 허용하려면 휴대폰 번호가 필요합니다.");
        }

        Address savedAddress = null;
        if (command.address() != null) {
            Address address = Address.of(
                    command.address().zipCode(),
                    command.address().roadAddress(),
                    command.address().jibunAddress(),
                    command.address().detailAddress(),
                    command.address().buildingName(),
                    command.address().siDo(),
                    command.address().siGunGu(),
                    command.address().eupMyeonDong(),
                    command.address().legalDongCode()
            );
            savedAddress = addressRepository.save(address);
        }

        MembershipGrade defaultMembershipGrade = resolveDefaultMembershipGrade(tenantId);

        Member member = Member.register(
                command.memberNo(),
                command.name(),
                command.birthDate(),
                command.calendarType(),
                command.gender(),
                normalizedPhone,
                command.email(),
                savedAddress,
                defaultMembershipGrade,
                normalizedWebId,
                statusCode,
                // 가입일시는 관리자 등록 시점의 실제 시각으로 세팅한다(고객 셀프 가입과 동일).
                Instant.now(),
                null,
                null,
                null,
                null,
                command.ci(),
                command.anniversaries()
        );

        try {
            Member saved = memberRepository.save(member);
            memberLedgerService.record(saved, MemberLedgerEventType.REGISTER, statusCode, statusCode);
            String generatedPassword = null;
            if (appLoginAllowed) {
                InitialPasswordResolution initialPasswordResolution = resolveInitialPassword(
                        command.initialPassword(),
                        command.autoGeneratePassword()
                );
                memberCredentialService.provision(
                        saved.getId(),
                        normalizedPhone,
                        saved.getEmail(),
                        initialPasswordResolution.password()
                );
                if (Boolean.TRUE.equals(command.sendInitialPasswordLink())) {
                    initialPasswordLinkSender.send(saved, initialPasswordResolution.password());
                }
                generatedPassword = initialPasswordResolution.generatedPassword();
            }
            return toResult(saved, generatedPassword);
        } catch (DataIntegrityViolationException e) {
            // 동시 등록(race condition)으로 애플리케이션 선검사를 통과한 뒤 DB 유니크 제약에 걸린 경우.
            if (isPhoneUniqueViolation(e)) {
                throw new MemberPhoneAlreadyExistsException();
            }
            throw new MemberAlreadyExistsException("unique constraint violated (memberNo/webId/ci)");
        }
    }

    private boolean isPhoneUniqueViolation(DataIntegrityViolationException e) {
        Throwable cause = e;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase().contains(PHONE_UNIQUE_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * 휴대폰/이메일 인증 완료 여부를 서버측에서 강제한다.
     * verification-required=false(개발 기본값)이면 검증을 건너뛰어 기존과 동일하게 동작한다.
     * 실제 인증 인프라 연동 전까지는 command의 phoneVerified/emailVerified가 채워지지 않을 수 있으므로,
     * 플래그를 켜기 전(운영 전환 시)에는 인증 상태를 채우는 로직이 함께 준비되어야 한다.
     */
    private void verifyContactVerification(RegisterCommand command) {
        if (!memberVerificationProperties.isVerificationRequired()) {
            return;
        }
        if (!Boolean.TRUE.equals(command.phoneVerified())) {
            throw MemberVerificationRequiredException.phoneNotVerified();
        }
        if (!Boolean.TRUE.equals(command.emailVerified())) {
            throw MemberVerificationRequiredException.emailNotVerified();
        }
    }

    private MembershipGrade resolveDefaultMembershipGrade(UUID tenantId) {
        return membershipGradeRepository.findByTenantIdAndName(tenantId, DEFAULT_MEMBERSHIP_GRADE_NAME)
                .or(() -> membershipGradeRepository.findAllByTenantIdOrderByLevelAsc(tenantId).stream().findFirst())
                .orElse(null);
    }

    @Override
    @Transactional
    public MemberResult updateMyProfile(UUID memberId, UpdateInfoCommand command) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndId(tenantId, memberId)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));
        return updateMemberInfo(member, command);
    }

    @Override
    @Transactional
    public MemberResult updateInfo(String memberNo, UpdateInfoCommand command) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));
        return updateMemberInfo(member, command);
    }

    private MemberResult updateMemberInfo(Member member, UpdateInfoCommand command) {
        String beforeStatus = member.getStatusCode();

        Address savedAddress = null;
        if (command.address() != null) {
            Address address = Address.of(
                    command.address().zipCode(),
                    command.address().roadAddress(),
                    command.address().jibunAddress(),
                    command.address().detailAddress(),
                    command.address().buildingName(),
                    command.address().siDo(),
                    command.address().siGunGu(),
                    command.address().eupMyeonDong(),
                    command.address().legalDongCode()
            );
            savedAddress = addressRepository.save(address);
        }

        member.updateInfo(
                command.name(),
                command.birthDate(),
                command.calendarType(),
                command.gender(),
                command.phoneNumber(),
                command.email(),
                savedAddress,
                command.webId(),
                command.ci(),
                command.anniversaries()
        );

        try {
            Member saved = memberRepository.save(member);
            if (memberCredentialService.findByMemberId(saved.getId()).isPresent()) {
                if (normalizePhoneOrNull(command.phoneNumber()) == null) {
                    throw new IllegalArgumentException("앱 로그인 설정이 있는 회원은 휴대폰 번호를 비울 수 없습니다.");
                }
                memberCredentialService.syncProfile(saved.getId(), saved.getPhoneNumber(), saved.getEmail());
            }
            memberLedgerService.record(saved, MemberLedgerEventType.UPDATE_INFO, beforeStatus, saved.getStatusCode());
            return toResult(saved, null);
        } catch (DataIntegrityViolationException e) {
            throw new MemberAlreadyExistsException("unique constraint violated (webId/ci)");
        }
    }

    @Override
    @Transactional
    public MemberResult updateStatus(String memberNo, UpdateStatusCommand command, UUID changedBy) {
        UUID tenantId = TenantContext.requireTenantId();
        String newStatus = command.statusCode();
        validateStatusCode(tenantId, newStatus);

        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));

        // 최소 방어: 이미 완전 탈퇴(WITHDRAWN)된 회원은 어떤 상태로도 재변경 불가.
        if (MemberStatusCodes.WITHDRAWN.equals(member.getStatusCode())) {
            throw new InvalidMemberStatusException("이미 탈퇴 처리된 회원은 상태를 변경할 수 없습니다.");
        }

        String beforeStatus = member.getStatusCode();
        Instant now = Instant.now();

        // 목표 상태별 날짜 필드 전이 규칙.
        // 기본은 모든 날짜 null 클리어, 각 상태에서 필요한 필드만 세팅/보존한다.
        Instant dormantAt = null;
        Instant suspendedAt = null;
        Instant withdrawRequestedAt = null;
        Instant withdrawnAt = null;

        if (MemberStatusCodes.DORMANT.equals(newStatus)) {
            // 휴면: dormantAt 현재 시각(기존 로직 - command 값이 있으면 우선)
            dormantAt = (command.dormantAt() != null) ? toStartOfDayInstant(command.dormantAt()) : now;
        } else if (MemberStatusCodes.SUSPENDED.equals(newStatus)) {
            // 정지: suspendedAt 를 현재 시각으로 세팅(기존값 있으면 보존)
            suspendedAt = (member.getSuspendedAt() != null) ? member.getSuspendedAt() : now;
        } else if (MemberStatusCodes.WITHDRAW_REQUESTED.equals(newStatus)) {
            // 탈퇴요청: withdrawRequestedAt 를 현재 시각으로 세팅(기존값 있으면 보존)
            withdrawRequestedAt = (member.getWithdrawRequestedAt() != null) ? member.getWithdrawRequestedAt() : now;
        } else if (MemberStatusCodes.WITHDRAWN.equals(newStatus)) {
            // 즉시탈퇴: 요청 흔적(withdrawRequestedAt)·휴면일시(dormantAt)·정지일시(suspendedAt)는 보존, withdrawnAt 현재 시각(기존값 있으면 보존)
            dormantAt = member.getDormantAt();
            suspendedAt = member.getSuspendedAt();
            withdrawRequestedAt = member.getWithdrawRequestedAt();
            withdrawnAt = (member.getWithdrawnAt() != null) ? member.getWithdrawnAt() : now;
        }
        // ACTIVE(철회 포함): 모든 날짜 필드 null 클리어(초기값 그대로)

        member.updateStatus(newStatus, dormantAt, suspendedAt, withdrawRequestedAt, withdrawnAt);
        Member saved = memberRepository.save(member);
        memberLedgerService.record(saved, MemberLedgerEventType.UPDATE_STATUS, beforeStatus, saved.getStatusCode());
        recordStatusChangeIfChanged(tenantId, saved, changedBy, beforeStatus, command.reason());

        // 최종 탈회(WITHDRAWN)로 전이한 경우에만 잔여 포인트를 전량 소각한다. 같은 트랜잭션에서 원자적으로 처리한다.
        // WITHDRAW_REQUESTED(탈회요청)는 여기에 해당하지 않으므로 소각되지 않는다(30일 유예 중 취소 가능).
        if (MemberStatusCodes.WITHDRAWN.equals(newStatus)) {
            pointService.burnAll(saved.getId(), withdrawnAt, WITHDRAW_BURN_REASON, WITHDRAW_BURN_SOURCE);
        }
        return toResult(saved, null);
    }

    @Override
    @Transactional
    public MemberResult withdraw(String memberNo, WithdrawCommand command, UUID changedBy) {
        UUID tenantId = TenantContext.requireTenantId();
        validateStatusCode(tenantId, MemberStatusCodes.WITHDRAWN);

        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));

        // 최소 방어: 이미 완전 탈퇴(WITHDRAWN)된 회원은 어떤 상태로도 재변경 불가.
        if (MemberStatusCodes.WITHDRAWN.equals(member.getStatusCode())) {
            throw new InvalidMemberStatusException("이미 탈퇴 처리된 회원은 상태를 변경할 수 없습니다.");
        }

        String beforeStatus = member.getStatusCode();
        Instant withdrawnAt = (command.withdrawnAt() != null) ? toStartOfDayInstant(command.withdrawnAt()) : Instant.now();
        member.updateStatus(MemberStatusCodes.WITHDRAWN, member.getDormantAt(), member.getSuspendedAt(), member.getWithdrawRequestedAt(), withdrawnAt);

        Member saved = memberRepository.save(member);
        memberLedgerService.record(saved, MemberLedgerEventType.WITHDRAW, beforeStatus, saved.getStatusCode());
        recordStatusChangeIfChanged(tenantId, saved, changedBy, beforeStatus, command.reason());

        // 최종 탈회 확정 → 잔여 포인트 전량 소각. 같은 트랜잭션에서 원자적으로 처리한다.
        pointService.burnAll(saved.getId(), withdrawnAt, WITHDRAW_BURN_REASON, WITHDRAW_BURN_SOURCE);
        return toResult(saved, null);
    }

    /**
     * 회원 상태가 실제로 변경된 경우에만 상태 변경 이력을 남긴다.
     * (원장 기록(memberLedgerService.record)과 별개로, 상태 전이 이력을 독립적으로 기록한다.)
     */
    private void recordStatusChangeIfChanged(
            UUID tenantId,
            Member saved,
            UUID changedBy,
            String beforeStatus,
            String reason
    ) {
        String afterStatus = saved.getStatusCode();
        if (afterStatus.equals(beforeStatus)) {
            return;
        }
        memberStatusHistoryRepository.save(
                MemberStatusHistory.of(tenantId, saved.getId(), changedBy, beforeStatus, afterStatus, reason)
        );
    }

    @Override
    @Transactional
    public AppLoginResult updateAppLogin(String memberNo, UpdateAppLoginCommand command) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));

        if (!command.enabled()) {
            memberCredentialService.disable(member.getId());
            return new AppLoginResult(member.getMemberNo(), false, null, null);
        }

        String normalizedPhone = normalizePhoneOrNull(member.getPhoneNumber());
        if (normalizedPhone == null) {
            throw new IllegalArgumentException("앱 로그인을 활성화하려면 휴대폰 번호가 필요합니다.");
        }

        InitialPasswordResolution initialPasswordResolution = resolveInitialPassword(
                command.initialPassword(),
                command.autoGeneratePassword()
        );
        MemberCredentialService.CredentialInfo credentialInfo = memberCredentialService.provision(
                member.getId(),
                normalizedPhone,
                member.getEmail(),
                initialPasswordResolution.password()
        );

        return new AppLoginResult(
                member.getMemberNo(),
                credentialInfo.appLoginEnabled(),
                credentialInfo.loginId(),
                initialPasswordResolution.generatedPassword()
        );
    }

    private void validateStatusCode(UUID tenantId, String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            throw new InvalidMemberStatusException("statusCode must not be blank");
        }

        CommonCode code = commonCodeRepository
                .findByTenantIdAndCodeGroupAndCodeAndActiveIsTrue(tenantId, MemberStatusCodes.GROUP, statusCode.trim())
                .orElse(null);

        if (code == null) {
            throw new InvalidMemberStatusException("Invalid member status code: " + statusCode);
        }
    }

    private void validateWebId(String webId) {
        String v = normalizeWebIdOrNull(webId);
        if (v == null) return;
        if (!WEB_ID_PATTERN.matcher(v).matches()) {
            throw new IllegalArgumentException("webId must match ^[A-Za-z0-9_-]+$");
        }
    }

    private String normalizeWebIdOrNull(String rawWebId) {
        if (rawWebId == null) return null;
        String trimmed = rawWebId.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 관리자가 입력한 날짜(LocalDate)를 Instant 로 변환한다.
     * DB 컬럼이 timestamptz 로 승격되었고 마이그레이션에서 기존 DATE 를 00:00 UTC 로 캐스팅했으므로,
     * 입력 날짜도 동일하게 해당 일자의 00:00 UTC 시각으로 통일한다.
     */
    private Instant toStartOfDayInstant(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private String normalizePhoneOrNull(String rawPhoneNumber) {
        if (rawPhoneNumber == null) return null;
        String digits = rawPhoneNumber.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    private MemberResult toResult(Member member, String generatedPassword) {
        return new MemberResult(
                member.getId(),
                member.getMemberNo(),
                member.getName(),
                member.getBirthDate(),
                member.getCalendarType(),
                member.getGender(),
                member.getPhoneNumber(),
                member.getEmail(),
                toAddressResponse(member.getAddress()),
                member.getWebId(),
                member.getStatusCode(),
                member.getJoinedAt(),
                member.getDormantAt(),
                member.getSuspendedAt(),
                member.getWithdrawRequestedAt(),
                member.getWithdrawnAt(),
                member.getCi(),
                member.getAnniversaries(),
                memberCredentialService.isAppLoginEnabled(member.getId()),
                memberCredentialService.getLoginId(member.getId()),
                generatedPassword
        );
    }

    private InitialPasswordResolution resolveInitialPassword(String initialPassword, Boolean autoGeneratePassword) {
        if (initialPassword != null && !initialPassword.isBlank()) {
            return new InitialPasswordResolution(initialPassword.trim(), null);
        }
        if (Boolean.TRUE.equals(autoGeneratePassword)) {
            String generatedPassword = "App" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            return new InitialPasswordResolution(generatedPassword, generatedPassword);
        }
        throw new IllegalArgumentException("초기 비밀번호를 입력하거나 자동 생성 옵션을 선택해주세요.");
    }

    private record InitialPasswordResolution(
            String password,
            String generatedPassword
    ) {
    }

    private AddressResponse toAddressResponse(Address address) {
        if (address == null) return null;
        return new AddressResponse(
                address.getId(),
                address.getZipCode(),
                address.getRoadAddress(),
                address.getJibunAddress(),
                address.getDetailAddress(),
                address.getBuildingName(),
                address.getSiDo(),
                address.getSiGunGu(),
                address.getEupMyeonDong(),
                address.getLegalDongCode()
        );
    }

}

