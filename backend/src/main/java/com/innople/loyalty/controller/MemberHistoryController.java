package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.MemberQueryDtos;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.repository.MemberGradeHistoryRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.MemberStatusHistoryRepository;
import com.innople.loyalty.service.member.MemberExceptions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 관리자용 회원 상태/등급 변경 이력 조회 컨트롤러.
 * <p>{@link MemberQueryController} 는 전 메서드 무가드(permitAll)이므로, 관리자 권한이 필요한 이 조회는
 * 별도 컨트롤러로 분리하고 각 메서드 첫 줄에서 {@code adminRoleResolver.requireAtLeast(..., ADMIN)} 로 방어한다.
 * SecurityConfig 상 {@code /api/v1/members/{memberNo}/...} 경로는 두 인증 필터 모두 skip 되어 무인증으로 열리므로
 * 이 수동 가드가 유일한 방어선이다. 경로를 {@code /me/} 아래에 두지 않는다(회원 셀프 조회와 시맨틱이 다르다).
 */
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberHistoryController {

    private final AdminRoleResolver adminRoleResolver;
    private final MemberRepository memberRepository;
    private final MemberStatusHistoryRepository memberStatusHistoryRepository;
    private final MemberGradeHistoryRepository memberGradeHistoryRepository;

    @GetMapping("/{memberNo}/status-histories")
    public List<MemberQueryDtos.MemberStatusHistoryResponse> statusHistories(
            @PathVariable String memberNo,
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        UUID tenantId = TenantContext.requireTenantId();
        Member member = findMember(tenantId, memberNo);
        return memberStatusHistoryRepository.findStatusHistoryView(
                tenantId,
                member.getId(),
                PageRequest.of(0, clampLimit(limit))
        );
    }

    @GetMapping("/{memberNo}/grade-histories")
    public List<MemberQueryDtos.MemberGradeHistoryResponse> gradeHistories(
            @PathVariable String memberNo,
            @RequestParam(defaultValue = "50") int limit,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        UUID tenantId = TenantContext.requireTenantId();
        Member member = findMember(tenantId, memberNo);
        return memberGradeHistoryRepository.findGradeHistoryView(
                tenantId,
                member.getId(),
                PageRequest.of(0, clampLimit(limit))
        );
    }

    private Member findMember(UUID tenantId, String memberNo) {
        return memberRepository.findByTenantIdAndMemberNo(tenantId, memberNo)
                .orElseThrow(() -> new MemberExceptions.MemberNotFoundException("member not found: " + memberNo));
    }

    // 기존 sibling(ledgers) 관례: 기본 50, 상한 200.
    private int clampLimit(int limit) {
        return Math.min(Math.max(limit, 1), 200);
    }
}
