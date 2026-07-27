package com.innople.loyalty.controller;

import com.innople.loyalty.config.AdminRoleResolver;
import com.innople.loyalty.config.ApiAuditLogInterceptor;
import com.innople.loyalty.controller.dto.MemberDtos;
import com.innople.loyalty.domain.member.HistoryActorType;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.service.member.MemberDuplicationService;
import com.innople.loyalty.service.member.MemberNumberService;
import com.innople.loyalty.service.member.MemberResult;
import com.innople.loyalty.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberNumberService memberNumberService;
    private final MemberDuplicationService memberDuplicationService;
    private final AdminRoleResolver adminRoleResolver;

    @GetMapping("/member-no/suggest")
    public MemberDtos.SuggestMemberNoResponse suggestMemberNo(@RequestParam @NotBlank String phoneNumber) {
        MemberNumberService.SuggestedMemberNo r = memberNumberService.suggestForPhoneNumber(phoneNumber);
        return new MemberDtos.SuggestMemberNoResponse(r.memberNo());
    }

    @GetMapping("/duplicate-check")
    public MemberDtos.DuplicationCheckResponse checkDuplicate(
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String webId
    ) {
        MemberDuplicationService.DuplicationResult r = memberDuplicationService.check(memberNo, phoneNumber, webId);
        return new MemberDtos.DuplicationCheckResponse(r.memberNoDuplicated(), r.phoneNumberDuplicated(), r.webIdDuplicated());
    }

    @PostMapping
    public MemberDtos.MemberResponse register(@Valid @RequestBody MemberDtos.RegisterRequest request, HttpServletRequest httpRequest) {
        MemberResult result = memberService.register(new MemberService.RegisterCommand(
                request.memberNo(),
                request.name(),
                request.birthDate(),
                request.calendarType(),
                request.gender(),
                request.phoneNumber(),
                request.email(),
                request.address(),
                request.webId(),
                request.statusCode(),
                request.ci(),
                request.anniversaries(),
                request.appLoginAllowed(),
                request.initialPassword(),
                request.autoGeneratePassword(),
                request.sendInitialPasswordLink(),
                request.phoneVerified(),
                request.emailVerified()
        ));
        setMemberAuditMessage(httpRequest, "회원 생성", result.memberNo());
        return toResponse(result);
    }

    @PutMapping("/{memberNo}")
    public MemberDtos.MemberResponse updateInfo(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.UpdateInfoRequest request,
            HttpServletRequest httpRequest
    ) {
        MemberResult result = memberService.updateInfo(memberNo, new MemberService.UpdateInfoCommand(
                request.name(),
                request.birthDate(),
                request.calendarType(),
                request.gender(),
                request.phoneNumber(),
                request.email(),
                request.address(),
                request.webId(),
                request.ci(),
                request.anniversaries()
        ));
        setMemberAuditMessage(httpRequest, "회원 정보 변경", memberNo);
        return toResponse(result);
    }

    @PutMapping("/{memberNo}/status")
    public MemberDtos.MemberResponse updateStatus(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.UpdateStatusRequest request,
            HttpServletRequest httpRequest
    ) {
        // 기본 권한선: ADMIN 이상만 상태 변경 가능. resolve 실패(미인증)는 여기서 403 으로 차단되어
        // 서비스의 changedBy null 가드(400)에 도달하기 전에 처리된다(서비스 가드는 방어선으로 유지).
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        // 즉시탈퇴(WITHDRAWN)는 SUPER_ADMIN 전용. 그 외 상태는 ADMIN 이상 유지.
        if (MemberStatusCodes.WITHDRAWN.equals(request.statusCode())) {
            adminRoleResolver.requireAtLeast(httpRequest, AdminRole.SUPER_ADMIN);
        }
        AdminUser actor = adminRoleResolver.resolve(httpRequest);
        UUID changedBy = (actor != null) ? actor.getId() : null;
        MemberResult result = memberService.updateStatus(memberNo, new MemberService.UpdateStatusCommand(
                request.statusCode(),
                request.dormantAt(),
                request.reason()
        ), changedBy);
        setMemberAuditMessage(httpRequest, "회원 상태 변경", memberNo);
        return toResponse(result);
    }

    @PutMapping("/{memberNo}/grade")
    public MemberDtos.MemberResponse updateGrade(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.UpdateGradeRequest request,
            HttpServletRequest httpRequest
    ) {
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        AdminUser actor = adminRoleResolver.resolve(httpRequest);
        UUID changedBy = (actor != null) ? actor.getId() : null;
        MemberResult result = memberService.updateGrade(memberNo, new MemberService.UpdateGradeCommand(
                request.gradeId(),
                request.reason()
        ), changedBy);
        setMemberAuditMessage(httpRequest, "회원 등급 변경", memberNo);
        return toResponse(result);
    }

    @PutMapping("/{memberNo}/withdraw")
    public MemberDtos.MemberResponse withdraw(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.WithdrawRequest request,
            HttpServletRequest httpRequest
    ) {
        // 탈퇴 처리는 ADMIN 이상만 가능. 미인증(resolve 실패)은 403 으로 차단(서비스 changedBy 가드는 방어선으로 유지).
        adminRoleResolver.requireAtLeast(httpRequest, AdminRole.ADMIN);
        AdminUser actor = adminRoleResolver.resolve(httpRequest);
        UUID changedBy = (actor != null) ? actor.getId() : null;
        MemberResult result = memberService.withdraw(memberNo, new MemberService.WithdrawCommand(
                request.withdrawnAt(),
                request.reason()
        ), changedBy, HistoryActorType.ADMIN);
        setMemberAuditMessage(httpRequest, "회원 탈퇴", memberNo);
        return toResponse(result);
    }

    @PutMapping("/{memberNo}/app-login")
    public MemberDtos.AppLoginResponse updateAppLogin(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.UpdateAppLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        MemberService.AppLoginResult result = memberService.updateAppLogin(
                memberNo,
                new MemberService.UpdateAppLoginCommand(
                        request.enabled(),
                        request.initialPassword(),
                        request.autoGeneratePassword()
                )
        );
        setMemberAuditMessage(httpRequest, request.enabled() ? "앱 로그인 활성화" : "앱 로그인 비활성화", memberNo);
        return new MemberDtos.AppLoginResponse(
                result.memberNo(),
                result.appLoginEnabled(),
                result.appLoginId(),
                result.generatedPassword()
        );
    }

    private void setMemberAuditMessage(HttpServletRequest httpRequest, String action, String memberNo) {
        AdminUser admin = adminRoleResolver.resolve(httpRequest);
        String adminName = admin != null ? admin.getName() : "관리자";
        ApiAuditLogInterceptor.setAuditMessage(httpRequest, "%s (%s → 회원 %s)".formatted(action, adminName, memberNo));
    }

    private MemberDtos.MemberResponse toResponse(MemberResult r) {
        return new MemberDtos.MemberResponse(
                r.id(),
                r.memberNo(),
                r.name(),
                r.birthDate(),
                r.calendarType(),
                r.gender(),
                r.phoneNumber(),
                r.email(),
                r.address(),
                r.webId(),
                r.statusCode(),
        r.joinedAt(),
        r.dormantAt(),
        r.suspendedAt(),
        r.withdrawRequestedAt(),
        r.withdrawnAt(),
        r.ci(),
                r.anniversaries(),
                r.appLoginEnabled(),
                r.appLoginId(),
                r.generatedPassword(),
                r.gradeId(),
                r.gradeName()
        );
    }
}

