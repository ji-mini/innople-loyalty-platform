package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.service.stamp.StampService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stamps")
@RequiredArgsConstructor
public class StampApiController {

    private final StampService stampService;

    /** POS 연동: 구매 금액 기준 스탬프 적립(멱등: 동일 posOrderReferenceId 재호출 시 중복 적립 없음). */
    @PostMapping("/earn/pos")
    public StampDtos.StampPosEarnResponse earnPos(@Valid @RequestBody StampDtos.StampPosEarnRequest request) {
        StampService.PosEarnResult r = stampService.earnFromPos(
                request.memberId(),
                request.purchaseAmountWon(),
                request.posOrderReferenceId()
        );
        return new StampDtos.StampPosEarnResponse(
                r.ledgerId(),
                r.stampsEarned(),
                r.currentBalance(),
                r.idempotentReplay()
        );
    }

    /** 고객 앱: MANUAL 정책일 때 쿠폰 받기(스탬프 차감 + 발급 기록). */
    @PostMapping("/claim")
    public StampDtos.StampClaimResponse claim(@Valid @RequestBody StampDtos.StampClaimRequest request) {
        StampService.ClaimResult r = stampService.claimCouponForMember(request.memberId());
        return new StampDtos.StampClaimResponse(r.ledgerId(), r.couponIssueId(), r.currentBalance());
    }
}
