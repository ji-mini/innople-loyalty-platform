package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.config.auth.MemberJwtTokenProvider;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberCredential;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.member.MembershipGrade;
import com.innople.loyalty.domain.points.PointAccount;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.MembershipGradeRepository;
import com.innople.loyalty.repository.PointAccountRepository;
import com.innople.loyalty.service.member.MemberLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberAuthServiceImpl implements MemberAuthService {
    private static final Pattern PHONE_INPUT_REGEX = Pattern.compile("^[0-9+\\-()\\s]{9,30}$");

    private final MemberRepository memberRepository;
    private final MemberCredentialService memberCredentialService;
    private final MembershipGradeRepository membershipGradeRepository;
    private final PointAccountRepository pointAccountRepository;
    private final MemberJwtTokenProvider memberJwtTokenProvider;
    private final MemberLedgerService memberLedgerService;
    private final MemberLoginHistoryService memberLoginHistoryService;

    @Override
    @Transactional
    public MemberAuthResult signup(MemberSignupCommand command) {
        UUID tenantId = TenantContext.requireTenantId();
        String email = normalizeEmailOrNull(command.email());
        String phone = normalizePhone(command.phone());

        if (memberRepository.existsByTenantIdAndPhoneNumber(tenantId, phone)) {
            throw new MemberAuthExceptions.MemberAlreadyExistsException("이미 사용 중인 휴대폰 번호입니다.");
        }
        if (email != null && memberRepository.existsByTenantIdAndEmail(tenantId, email)) {
            throw new MemberAuthExceptions.MemberAlreadyExistsException("이미 사용 중인 이메일입니다.");
        }

        MembershipGrade grade = membershipGradeRepository
                .findAllByTenantIdOrderByLevelAsc(tenantId)
                .stream()
                .findFirst()
                .orElse(null);

        String memberNo = generateMemberNo(tenantId);
        Member savedMember;
        try {
            Member member = Member.register(
                    memberNo,
                    command.name().trim(),
                    null,
                    null,
                    null,
                    phone,
                    email,
                    null,
                    grade,
                    null,
                    MemberStatusCodes.ACTIVE,
                    LocalDate.now(),
                    null,
                    null,
                    null,
                    null
            );
            savedMember = memberRepository.save(member);
            memberCredentialService.provision(
                    savedMember.getId(),
                    phone,
                    email,
                    command.password()
            );
            pointAccountRepository.save(new PointAccount(savedMember.getId()));
            memberLedgerService.record(
                    savedMember,
                    MemberLedgerEventType.REGISTER,
                    MemberStatusCodes.ACTIVE,
                    MemberStatusCodes.ACTIVE
            );
        } catch (DataIntegrityViolationException ex) {
            throw new MemberAuthExceptions.MemberAlreadyExistsException("회원 생성 중 중복 데이터가 발견되었습니다.");
        }

        String accessToken = memberJwtTokenProvider.createAccessToken(tenantId, savedMember.getId(), phone);
        return toResult(savedMember, grade, accessToken, 0L);
    }

    @Override
    @Transactional
    public MemberAuthResult login(MemberLoginCommand command, MemberLoginContext context) {
        UUID tenantId = TenantContext.requireTenantId();
        String phoneNumber = normalizePhone(command.phoneNumber());
        if (command.password() == null || command.password().isBlank()) {
            throw new MemberAuthExceptions.InvalidCredentialsException("휴대폰 번호 또는 비밀번호가 올바르지 않습니다.");
        }

        MemberCredential credential = memberCredentialService.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new MemberAuthExceptions.InvalidCredentialsException("휴대폰 번호 또는 비밀번호가 올바르지 않습니다."));
        if (credential.isDeleted()) {
            throw new MemberAuthExceptions.AppLoginDisabledException("앱 로그인이 비활성화된 회원입니다.");
        }

        MemberCredential activeCredential = memberCredentialService.findActiveByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new MemberAuthExceptions.InvalidCredentialsException("휴대폰 번호 또는 비밀번호가 올바르지 않습니다."));

        if (!memberCredentialMatches(command.password(), activeCredential.getPasswordHash())) {
            throw new MemberAuthExceptions.InvalidCredentialsException("휴대폰 번호 또는 비밀번호가 올바르지 않습니다.");
        }

        Member member = memberRepository.findByTenantIdAndIdWithMembershipGrade(tenantId, activeCredential.getMemberId())
                .orElseThrow(() -> new MemberAuthExceptions.InvalidCredentialsException("회원 정보가 존재하지 않습니다."));

        long pointBalance = pointAccountRepository.findByTenantIdAndMemberId(tenantId, member.getId())
                .map(PointAccount::getCurrentBalance)
                .orElse(0L);

        memberLoginHistoryService.recordSuccess(
                member,
                activeCredential.getPhoneNumber(),
                context != null ? context.deviceName() : null,
                context != null ? context.osName() : null,
                context != null ? context.ip() : null,
                context != null ? context.userAgent() : null
        );
        String accessToken = memberJwtTokenProvider.createAccessToken(tenantId, member.getId(), activeCredential.getPhoneNumber());
        return toResult(member, member.getMembershipGrade(), accessToken, pointBalance);
    }

    private MemberAuthResult toResult(Member member, MembershipGrade grade, String accessToken, long pointBalance) {
        return new MemberAuthResult(
                accessToken,
                member.getId(),
                member.getMemberNo(),
                member.getName(),
                member.getEmail(),
                member.getPhoneNumber(),
                grade != null ? grade.getName() : null,
                pointBalance
        );
    }

    private String generateMemberNo(UUID tenantId) {
        for (int i = 0; i < 5; i++) {
            String memberNo = "M" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
            if (!memberRepository.existsByTenantIdAndMemberNo(tenantId, memberNo)) {
                return memberNo;
            }
        }
        throw new IllegalStateException("memberNo 생성에 실패했습니다.");
    }

    private String normalizeEmailOrNull(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            return null;
        }
        String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("email 형식이 올바르지 않습니다.");
        }
        return normalized;
    }

    private String normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        String normalized = rawPhone.trim();
        if (!PHONE_INPUT_REGEX.matcher(normalized).matches()) {
            throw new IllegalArgumentException("phone 형식이 올바르지 않습니다.");
        }
        return normalized.replaceAll("\\D", "");
    }

    private boolean memberCredentialMatches(String rawPassword, String passwordHash) {
        return passwordHash != null && org.springframework.security.crypto.bcrypt.BCrypt.checkpw(rawPassword, passwordHash);
    }
}
