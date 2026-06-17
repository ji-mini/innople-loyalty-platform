package com.innople.loyalty.service.member;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberDtos.AddressRequest;
import com.innople.loyalty.controller.dto.MemberDtos.AddressResponse;
import com.innople.loyalty.domain.code.CommonCode;
import com.innople.loyalty.domain.member.Address;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.AddressRepository;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.MembershipGradeRepository;
import com.innople.loyalty.service.memberauth.MemberCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.innople.loyalty.service.member.MemberExceptions.InvalidMemberStatusException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberAlreadyExistsException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberNotFoundException;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private static final String DEFAULT_MEMBERSHIP_GRADE_NAME = "기본등급";

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final MemberLedgerService memberLedgerService;
    private final MembershipGradeRepository membershipGradeRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final MemberCredentialService memberCredentialService;
    private final InitialPasswordLinkSender initialPasswordLinkSender;
    private static final Pattern WEB_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    @Override
    @Transactional
    public MemberResult register(RegisterCommand command) {
        UUID tenantId = TenantContext.requireTenantId();

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
            throw new MemberAlreadyExistsException("phoneNumber already exists");
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
                (command.joinedAt() != null) ? command.joinedAt() : LocalDate.now(),
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
            throw new MemberAlreadyExistsException("unique constraint violated (memberNo/webId/ci)");
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
    public MemberResult updateStatus(String memberNo, UpdateStatusCommand command) {
        UUID tenantId = TenantContext.requireTenantId();
        String newStatus = command.statusCode();
        validateStatusCode(tenantId, newStatus);

        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));

        String beforeStatus = member.getStatusCode();
        LocalDate dormantAt = command.dormantAt();
        LocalDate withdrawnAt = member.getWithdrawnAt();

        if (MemberStatusCodes.DORMANT.equals(newStatus) && dormantAt == null) {
            dormantAt = LocalDate.now();
        }
        if (!MemberStatusCodes.DORMANT.equals(newStatus)) {
            dormantAt = null;
        }
        if (MemberStatusCodes.WITHDRAWN.equals(newStatus) && withdrawnAt == null) {
            withdrawnAt = LocalDate.now();
        }

        member.updateStatus(newStatus, dormantAt, withdrawnAt);
        Member saved = memberRepository.save(member);
        memberLedgerService.record(saved, MemberLedgerEventType.UPDATE_STATUS, beforeStatus, saved.getStatusCode());
        return toResult(saved, null);
    }

    @Override
    @Transactional
    public MemberResult withdraw(String memberNo, WithdrawCommand command) {
        UUID tenantId = TenantContext.requireTenantId();
        validateStatusCode(tenantId, MemberStatusCodes.WITHDRAWN);

        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));

        String beforeStatus = member.getStatusCode();
        LocalDate withdrawnAt = (command.withdrawnAt() != null) ? command.withdrawnAt() : LocalDate.now();
        member.updateStatus(MemberStatusCodes.WITHDRAWN, member.getDormantAt(), withdrawnAt);

        Member saved = memberRepository.save(member);
        memberLedgerService.record(saved, MemberLedgerEventType.WITHDRAW, beforeStatus, saved.getStatusCode());
        return toResult(saved, null);
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

