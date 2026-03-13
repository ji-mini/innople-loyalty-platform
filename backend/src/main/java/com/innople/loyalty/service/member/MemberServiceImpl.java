package com.innople.loyalty.service.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberDtos.AddressRequest;
import com.innople.loyalty.controller.dto.MemberDtos.AddressResponse;
import com.innople.loyalty.domain.code.CommonCode;
import com.innople.loyalty.domain.member.Address;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedger;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.AddressRepository;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.repository.MemberLedgerRepository;
import com.innople.loyalty.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.UUID;

import static com.innople.loyalty.service.member.MemberExceptions.InvalidMemberStatusException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberAlreadyExistsException;
import static com.innople.loyalty.service.member.MemberExceptions.MemberNotFoundException;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final MemberLedgerRepository memberLedgerRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final ObjectMapper objectMapper;
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

        Member member = Member.register(
                command.memberNo(),
                command.name(),
                command.birthDate(),
                command.calendarType(),
                command.gender(),
                normalizedPhone,
                command.email(),
                savedAddress,
                null,
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

    private MemberResult toResult(Member member) {
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
                member.getAnniversaries()
        );
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

    private String toSnapshotJson(Member member) {
        Address addr = member.getAddress();
        String addressSnapshot = addr != null
                ? Map.of(
                        "zipCode", addr.getZipCode(),
                        "roadAddress", addr.getRoadAddress(),
                        "jibunAddress", addr.getJibunAddress() != null ? addr.getJibunAddress() : "",
                        "detailAddress", addr.getDetailAddress() != null ? addr.getDetailAddress() : "",
                        "buildingName", addr.getBuildingName() != null ? addr.getBuildingName() : "",
                        "siDo", addr.getSiDo() != null ? addr.getSiDo() : "",
                        "siGunGu", addr.getSiGunGu() != null ? addr.getSiGunGu() : "",
                        "eupMyeonDong", addr.getEupMyeonDong() != null ? addr.getEupMyeonDong() : "",
                        "legalDongCode", addr.getLegalDongCode() != null ? addr.getLegalDongCode() : ""
                ).toString()
                : null;

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("memberNo", member.getMemberNo());
        snapshot.put("name", member.getName());
        snapshot.put("birthDate", member.getBirthDate());
        snapshot.put("calendarType", member.getCalendarType());
        snapshot.put("gender", member.getGender());
        snapshot.put("phoneNumber", member.getPhoneNumber());
        snapshot.put("email", member.getEmail());
        snapshot.put("address", addressSnapshot);
        snapshot.put("webId", member.getWebId());
        snapshot.put("statusCode", member.getStatusCode());
        snapshot.put("joinedAt", member.getJoinedAt());
        snapshot.put("dormantAt", member.getDormantAt());
        snapshot.put("withdrawnAt", member.getWithdrawnAt());
        snapshot.put("ci", member.getCi());
        snapshot.put("anniversaries", member.getAnniversaries());
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize member snapshot", e);
        }
    }
}

