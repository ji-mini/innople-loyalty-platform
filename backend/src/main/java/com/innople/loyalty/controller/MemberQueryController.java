package com.innople.loyalty.controller;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberQueryDtos;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedger;
import com.innople.loyalty.repository.MemberLedgerRepository;
import com.innople.loyalty.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberQueryController {

    private final MemberRepository memberRepository;
    private final MemberLedgerRepository memberLedgerRepository;

    @GetMapping
    public MemberQueryDtos.PagedResponse<MemberQueryDtos.MemberSummaryResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String statusCode,
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String webId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate joinedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "joinedAt").and(Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        Page<MemberRepository.MemberSummaryView> result = memberRepository.searchSummary(
                tenantId,
                normalize(keyword),
                normalize(statusCode),
                normalize(memberNo),
                normalize(phoneNumber),
                normalize(name),
                normalize(webId),
                joinedFrom,
                joinedTo,
                pageable
        );

        List<MemberQueryDtos.MemberSummaryResponse> items = result.getContent().stream().map(v -> new MemberQueryDtos.MemberSummaryResponse(
                v.getId(),
                v.getMemberNo(),
                v.getName(),
                v.getPointBalance(),
                v.getStatusCode(),
                v.getPhoneNumber(),
                v.getWebId(),
                v.getJoinedAt(),
                v.getDormantAt(),
                v.getWithdrawnAt()
        )).toList();

        return new MemberQueryDtos.PagedResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @GetMapping("/{memberNo}")
    public MemberQueryDtos.MemberDetailResponse get(@PathVariable String memberNo) {
        UUID tenantId = TenantContext.requireTenantId();
        Member member = memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));
        return toDetail(member);
    }

    @GetMapping("/{memberNo}/ledgers")
    public List<MemberQueryDtos.MemberLedgerResponse> ledgers(
            @PathVariable String memberNo,
            @RequestParam(defaultValue = "50") int limit
    ) {
        UUID tenantId = TenantContext.requireTenantId();
        int size = Math.min(Math.max(limit, 1), 200);

        List<MemberLedger> ledgers = memberLedgerRepository.findByTenantIdAndMemberNoOrderByCreatedAtDesc(
                tenantId,
                memberNo,
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ledgers.stream().map(l -> new MemberQueryDtos.MemberLedgerResponse(
                l.getId(),
                l.getMemberNo(),
                l.getEventType().name(),
                l.getStatusCodeBefore(),
                l.getStatusCodeAfter(),
                l.getCreatedAt()
        )).toList();
    }

    private MemberQueryDtos.MemberDetailResponse toDetail(Member m) {
        return new MemberQueryDtos.MemberDetailResponse(
                m.getId(),
                m.getMemberNo(),
                m.getName(),
                m.getBirthDate(),
                m.getCalendarType(),
                m.getGender(),
                m.getPhoneNumber(),
                m.getAddress(),
                m.getWebId(),
                m.getStatusCode(),
                m.getJoinedAt(),
                m.getDormantAt(),
                m.getWithdrawnAt(),
                m.getCi(),
                m.getAnniversaries()
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isBlank() ? null : t;
    }
}

