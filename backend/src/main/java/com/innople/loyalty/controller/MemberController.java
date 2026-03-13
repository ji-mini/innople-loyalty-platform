package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.MemberDtos;
import com.innople.loyalty.service.member.MemberDuplicationService;
import com.innople.loyalty.service.member.MemberNumberService;
import com.innople.loyalty.service.member.MemberResult;
import com.innople.loyalty.service.member.MemberService;
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

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberNumberService memberNumberService;
    private final MemberDuplicationService memberDuplicationService;

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
    public MemberDtos.MemberResponse register(@Valid @RequestBody MemberDtos.RegisterRequest request) {
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
                request.joinedAt(),
                request.ci(),
                request.anniversaries()
        ));
        return toResponse(result);
    }

    @PutMapping("/{memberNo}")
    public MemberDtos.MemberResponse updateInfo(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.UpdateInfoRequest request
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
        return toResponse(result);
    }

    @PutMapping("/{memberNo}/status")
    public MemberDtos.MemberResponse updateStatus(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.UpdateStatusRequest request
    ) {
        MemberResult result = memberService.updateStatus(memberNo, new MemberService.UpdateStatusCommand(
                request.statusCode(),
                request.dormantAt()
        ));
        return toResponse(result);
    }

    @PutMapping("/{memberNo}/withdraw")
    public MemberDtos.MemberResponse withdraw(
            @PathVariable String memberNo,
            @Valid @RequestBody MemberDtos.WithdrawRequest request
    ) {
        MemberResult result = memberService.withdraw(memberNo, new MemberService.WithdrawCommand(
                request.withdrawnAt(),
                request.reason()
        ));
        return toResponse(result);
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
                r.withdrawnAt(),
                r.ci(),
                r.anniversaries()
        );
    }
}

