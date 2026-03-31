package com.innople.loyalty.service.stamp;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.domain.coupon.CouponTemplate;
import com.innople.loyalty.repository.CouponTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponTemplateAdminService {

    private final CouponTemplateRepository couponTemplateRepository;

    @Transactional(readOnly = true)
    public List<StampDtos.CouponTemplateResponse> list() {
        UUID tenantId = TenantContext.requireTenantId();
        return couponTemplateRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StampDtos.CouponTemplateResponse get(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        CouponTemplate t = couponTemplateRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 템플릿을 찾을 수 없습니다."));
        return toResponse(t);
    }

    @Transactional
    public StampDtos.CouponTemplateResponse create(StampDtos.CouponTemplateCreateRequest request) {
        TenantContext.requireTenantId();
        CouponTemplate t = new CouponTemplate(
                request.name().trim(),
                request.description() != null ? request.description().trim() : null,
                request.active()
        );
        t = couponTemplateRepository.save(t);
        return toResponse(t);
    }

    @Transactional
    public StampDtos.CouponTemplateResponse update(UUID id, StampDtos.CouponTemplateUpdateRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        CouponTemplate t = couponTemplateRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 템플릿을 찾을 수 없습니다."));
        t.update(
                request.name().trim(),
                request.description() != null ? request.description().trim() : null,
                request.active()
        );
        return toResponse(t);
    }

    private StampDtos.CouponTemplateResponse toResponse(CouponTemplate t) {
        return new StampDtos.CouponTemplateResponse(
                t.getId(),
                t.getName(),
                t.getDescription(),
                t.isActive(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
