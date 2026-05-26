package com.innople.loyalty.service.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innople.loyalty.domain.member.Address;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedger;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.repository.MemberLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberLedgerServiceImpl implements MemberLedgerService {
    private final MemberLedgerRepository memberLedgerRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void record(Member member, MemberLedgerEventType eventType, String statusCodeBefore, String statusCodeAfter) {
        memberLedgerRepository.save(MemberLedger.of(
                member.getId(),
                member.getMemberNo(),
                eventType,
                statusCodeBefore,
                statusCodeAfter,
                toSnapshotJson(member)
        ));
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
