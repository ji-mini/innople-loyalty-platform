package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.MemberDtos;
import com.innople.loyalty.service.member.MemberResult;
import com.innople.loyalty.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public MemberDtos.MemberResponse register(@Valid @RequestBody MemberDtos.RegisterRequest request) {
        MemberResult result = memberService.register(new MemberService.RegisterCommand(
                request.memberNo(),
                request.name(),
                request.birthDate(),
                request.calendarType(),
                request.gender(),
                request.phoneNumber(),
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

