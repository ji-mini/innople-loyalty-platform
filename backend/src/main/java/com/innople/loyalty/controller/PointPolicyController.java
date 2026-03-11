package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.PointPolicyDtos;
import com.innople.loyalty.service.points.PointPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/point-policies")
@RequiredArgsConstructor
public class PointPolicyController {

    private final PointPolicyService pointPolicyService;

    @GetMapping
    public List<PointPolicyDtos.PointPolicyResponse> list() {
        return pointPolicyService.list().stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public PointPolicyDtos.PointPolicyResponse create(@Valid @RequestBody PointPolicyDtos.CreateRequest request) {
        return toResponse(pointPolicyService.create(
                request.pointType(),
                request.name(),
                request.validityDays(),
                request.enabled(),
                request.description()
        ));
    }

    @PutMapping("/{policyId}")
    public PointPolicyDtos.PointPolicyResponse update(
            @PathVariable UUID policyId,
            @Valid @RequestBody PointPolicyDtos.UpdateRequest request
    ) {
        return toResponse(pointPolicyService.update(
                policyId,
                request.pointType(),
                request.name(),
                request.validityDays(),
                request.enabled(),
                request.description()
        ));
    }

    private PointPolicyDtos.PointPolicyResponse toResponse(PointPolicyService.PointPolicyItem i) {
        return new PointPolicyDtos.PointPolicyResponse(
                i.id(),
                i.pointType(),
                i.name(),
                i.validityDays(),
                i.enabled(),
                i.description(),
                i.createdAt(),
                i.updatedAt()
        );
    }
}

