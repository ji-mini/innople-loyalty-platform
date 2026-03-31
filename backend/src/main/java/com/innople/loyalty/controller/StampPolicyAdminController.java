package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.service.stamp.StampPolicyAdminService;
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

/**
 * 스탬프 정책 관리 API ({@code /api/v1/admin/stamp-policies}).
 */
@RestController
@RequestMapping("/api/v1/admin/stamp-policies")
@RequiredArgsConstructor
public class StampPolicyAdminController {

    private final StampPolicyAdminService stampPolicyAdminService;

    @GetMapping
    public List<StampDtos.StampPolicyResponse> listPolicies() {
        return stampPolicyAdminService.list();
    }

    @GetMapping("/{id}")
    public StampDtos.StampPolicyResponse getPolicy(@PathVariable UUID id) {
        return stampPolicyAdminService.get(id);
    }

    @PostMapping
    public StampDtos.StampPolicyResponse createPolicy(@Valid @RequestBody StampDtos.StampPolicyCreateRequest request) {
        return stampPolicyAdminService.create(request);
    }

    @PutMapping("/{id}")
    public StampDtos.StampPolicyResponse updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody StampDtos.StampPolicyUpdateRequest request
    ) {
        return stampPolicyAdminService.update(id, request);
    }
}
