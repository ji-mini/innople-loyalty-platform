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

@RestController
@RequestMapping("/api/v1/admin/coupon-templates")
@RequiredArgsConstructor
public class CouponTemplateController {

    private final CouponTemplateAdminService couponTemplateAdminService;

    @GetMapping
    public List<StampDtos.CouponTemplateResponse> list() {
        return couponTemplateAdminService.list();
    }

    @GetMapping("/{id}")
    public StampDtos.CouponTemplateResponse get(@PathVariable UUID id) {
        return couponTemplateAdminService.get(id);
    }

    @PostMapping
    public StampDtos.CouponTemplateResponse create(@Valid @RequestBody StampDtos.CouponTemplateCreateRequest request) {
        return couponTemplateAdminService.create(request);
    }

    @PutMapping("/{id}")
    public StampDtos.CouponTemplateResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody StampDtos.CouponTemplateUpdateRequest request
    ) {
        return couponTemplateAdminService.update(id, request);
    }
}
