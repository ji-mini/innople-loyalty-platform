package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.service.stamp.CouponTemplateAdminService;
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
 * 스탬프 정책 + 쿠폰 템플릿 관리 API.
 * (동일 /api/v1/admin 하위에서 한 컨트롤러로 노출 — 배포/매핑 누락 시 404 방지)
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class StampPolicyController {

    private final StampPolicyAdminService stampPolicyAdminService;
    private final CouponTemplateAdminService couponTemplateAdminService;

    // --- 스탬프 정책: /api/v1/admin/stamp-policies ---

    @GetMapping("/stamp-policies")
    public List<StampDtos.StampPolicyResponse> listPolicies() {
        return stampPolicyAdminService.list();
    }

    @GetMapping("/stamp-policies/{id}")
    public StampDtos.StampPolicyResponse getPolicy(@PathVariable UUID id) {
        return stampPolicyAdminService.get(id);
    }

    @PostMapping("/stamp-policies")
    public StampDtos.StampPolicyResponse createPolicy(@Valid @RequestBody StampDtos.StampPolicyCreateRequest request) {
        return stampPolicyAdminService.create(request);
    }

    @PutMapping("/stamp-policies/{id}")
    public StampDtos.StampPolicyResponse updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody StampDtos.StampPolicyUpdateRequest request
    ) {
        return stampPolicyAdminService.update(id, request);
    }

    // --- 쿠폰 템플릿: /api/v1/admin/coupon-templates ---

    @GetMapping("/coupon-templates")
    public List<StampDtos.CouponTemplateResponse> listCouponTemplates() {
        return couponTemplateAdminService.list();
    }

    @GetMapping("/coupon-templates/{id}")
    public StampDtos.CouponTemplateResponse getCouponTemplate(@PathVariable UUID id) {
        return couponTemplateAdminService.get(id);
    }

    @PostMapping("/coupon-templates")
    public StampDtos.CouponTemplateResponse createCouponTemplate(@Valid @RequestBody StampDtos.CouponTemplateCreateRequest request) {
        return couponTemplateAdminService.create(request);
    }

    @PutMapping("/coupon-templates/{id}")
    public StampDtos.CouponTemplateResponse updateCouponTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody StampDtos.CouponTemplateUpdateRequest request
    ) {
        return couponTemplateAdminService.update(id, request);
    }
}
