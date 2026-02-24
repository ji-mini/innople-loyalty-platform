package com.innople.loyalty.service.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.code.CommonCode;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedger;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.repository.MemberLedgerRepository;
import com.innople.loyalty.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static com.innople.loyalty.service.member.MemberExceptions.InvalidMemberStatusException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberAlreadyExistsException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberNotFoundException;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberLedgerRepository memberLedgerRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public MemberResult register(RegisterCommand command) {
        UUID tenantId = TenantContext.requireTenantId();

        String statusCode = (command.statusCode() == null || command.statusCode().isBlank())
                ? MemberStatusCodes.NORMAL
                : command.statusCode().trim();

        validateStatusCode(tenantId, statusCode);

        if (memberRepository.existsByTenantIdAndMemberNo(tenantId, command.memberNo())) {
            throw new MemberAlreadyExistsException("memberNo already exists");
        }

        Member member = Member.register(
                command.memberNo(),
                command.name(),
                command.birthDate(),
                command.calendarType(),
                command.gender(),
                command.phoneNumber(),
                command.address(),
                command.webId(),
                statusCode,
                (command.joinedAt() != null) ? command.joinedAt() : LocalDate.now(),
                null,
                null,
                command.ci(),
                command.anniversaries()
        );

        try {
            Member saved = memberRepository.save(member);
            memberLedgerRepository.save(MemberLedger.of(
                    saved.getMemberNo(),
                    MemberLedgerEventType.REGISTER,
                    statusCode,
                    statusCode,
                    toSnapshotJson(saved)
            ));
            return toResult(saved);
        } catch (DataIntegrityViolationException e) {
            throw new MemberAlreadyExistsException("unique constraint violated (memberNo/webId/ci)");
        }
    }

    @Override
    @Transactional
    public MemberResult updateInfo(String memberNo, UpdateInfoCommand command) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberNotFoundException("member not found"));

        String beforeStatus = member.getStatusCode();
        member.updateInfo(
                command.name(),
                command.birthDate(),
                command.calendarType(),
                command.gender(),
                command.phoneNumber(),
                command.address(),
                command.webId(),
                command.ci(),
                command.anniversaries()
        );

        try {
            Member saved = memberRepository.save(member);
            memberLedgerRepository.save(MemberLedger.of(
                    saved.getMemberNo(),
                    MemberLedgerEventType.UPDATE_INFO,
                    beforeStatus,
                    saved.getStatusCode(),
                    toSnapshotJson(saved)
            ));
            return toResult(saved);
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
        memberLedgerRepository.save(MemberLedger.of(
                saved.getMemberNo(),
                MemberLedgerEventType.UPDATE_STATUS,
                beforeStatus,
                saved.getStatusCode(),
                toSnapshotJson(saved)
        ));
        return toResult(saved);
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
        memberLedgerRepository.save(MemberLedger.of(
                saved.getMemberNo(),
                MemberLedgerEventType.WITHDRAW,
                beforeStatus,
                saved.getStatusCode(),
                toSnapshotJson(saved)
        ));
        return toResult(saved);
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

    private MemberResult toResult(Member member) {
        return new MemberResult(
                member.getId(),
                member.getMemberNo(),
                member.getName(),
                member.getBirthDate(),
                member.getCalendarType(),
                member.getGender(),
                member.getPhoneNumber(),
                member.getAddress(),
                member.getWebId(),
                member.getStatusCode(),
                member.getJoinedAt(),
                member.getDormantAt(),
                member.getWithdrawnAt(),
                member.getCi(),
                member.getAnniversaries()
        );
    }

    private String toSnapshotJson(Member member) {
        Map<String, Object> snapshot = Map.of(
                "memberNo", member.getMemberNo(),
                "name", member.getName(),
                "birthDate", member.getBirthDate(),
                "calendarType", member.getCalendarType(),
                "gender", member.getGender(),
                "phoneNumber", member.getPhoneNumber(),
                "address", member.getAddress(),
                "webId", member.getWebId(),
                "statusCode", member.getStatusCode(),
                "joinedAt", member.getJoinedAt(),
                "dormantAt", member.getDormantAt(),
                "withdrawnAt", member.getWithdrawnAt(),
                "ci", member.getCi(),
                "anniversaries", member.getAnniversaries()
        );
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize member snapshot", e);
        }
    }
}

