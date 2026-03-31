package com.innople.loyalty.controller;

import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.service.stamp.CouponTemplateAdminService;
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
 * 쿠폰 템플릿 관리 API.
 * 구 클라이언트·프록시 호환을 위해 두 베이스 경로를 동일 핸들러에 매핑한다.
 */
@RestController
@RequestMapping({
        "/api/v1/admin/coupon-templates",
        "/api/v1/admin/stamp-policies/coupon-templates"
})
@RequiredArgsConstructor
public class CouponTemplateAdminController {

    private final CouponTemplateAdminService couponTemplateAdminService;

    @GetMapping
    public List<StampDtos.CouponTemplateResponse> listCouponTemplates() {
        return couponTemplateAdminService.list();
    }

    @GetMapping("/{id}")
    public StampDtos.CouponTemplateResponse getCouponTemplate(@PathVariable UUID id) {
        return couponTemplateAdminService.get(id);
    }

    @PostMapping
    public StampDtos.CouponTemplateResponse createCouponTemplate(
            @Valid @RequestBody StampDtos.CouponTemplateCreateRequest request
    ) {
        return couponTemplateAdminService.create(request);
    }

    @PutMapping("/{id}")
    public StampDtos.CouponTemplateResponse updateCouponTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody StampDtos.CouponTemplateUpdateRequest request
    ) {
        return couponTemplateAdminService.update(id, request);
    }
}
