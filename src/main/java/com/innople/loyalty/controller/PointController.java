package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.PointDtos;
import com.innople.loyalty.service.points.PointOperationResult;
import com.innople.loyalty.service.points.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/earn")
    public PointDtos.PointOperationResponse earn(@Valid @RequestBody PointDtos.EarnRequest request) {
        PointOperationResult result = pointService.earn(
                request.memberId(),
                request.amount(),
                request.expiresAt(),
                request.reason()
        );
        return new PointDtos.PointOperationResponse(
                result.ledgerId(),
                result.eventType(),
                result.amount(),
                result.currentBalance(),
                result.occurredAt()
        );
    }

    @PostMapping("/use")
    public PointDtos.PointOperationResponse use(@Valid @RequestBody PointDtos.UseRequest request) {
        PointOperationResult result = pointService.use(
                request.memberId(),
                request.amount(),
                request.reason()
        );
        return new PointDtos.PointOperationResponse(
                result.ledgerId(),
                result.eventType(),
                result.amount(),
                result.currentBalance(),
                result.occurredAt()
        );
    }

    @PostMapping("/expire/manual")
    public PointDtos.PointOperationResponse manualExpire(@Valid @RequestBody PointDtos.ManualExpireRequest request) {
        PointOperationResult result = pointService.manualExpire(
                request.memberId(),
                request.referenceAt(),
                request.reason()
        );
        return new PointDtos.PointOperationResponse(
                result.ledgerId(),
                result.eventType(),
                result.amount(),
                result.currentBalance(),
                result.occurredAt()
        );
    }
}

