package com.innople.loyalty.service.stamp;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.controller.dto.StampDtos;
import com.innople.loyalty.domain.coupon.CouponTemplate;
import com.innople.loyalty.domain.stamp.StampPolicy;
import com.innople.loyalty.repository.CouponTemplateRepository;
import com.innople.loyalty.repository.StampPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StampPolicyAdminService {

    private final StampPolicyRepository stampPolicyRepository;
    private final CouponTemplateRepository couponTemplateRepository;

    @Transactional(readOnly = true)
    public List<StampDtos.StampPolicyResponse> list() {
        UUID tenantId = TenantContext.requireTenantId();
        return stampPolicyRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StampDtos.StampPolicyResponse get(UUID id) {
        UUID tenantId = TenantContext.requireTenantId();
        StampPolicy p = stampPolicyRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("스탬프 정책을 찾을 수 없습니다."));
        return toResponse(p);
    }

    @Transactional
    public StampDtos.StampPolicyResponse create(StampDtos.StampPolicyCreateRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        validateCouponTemplate(tenantId, request.couponTemplateId());

        StampPolicy p = new StampPolicy(
                request.name().trim(),
                request.amountWonPerStamp(),
                request.stampsRequiredForCoupon(),
                request.couponTemplateId(),
                request.issuanceMode(),
                false
        );
        p = stampPolicyRepository.save(p);
        if (request.active()) {
            deactivateOthers(tenantId, p.getId());
            p.setActive(true);
            stampPolicyRepository.save(p);
        }
        return toResponse(p);
    }

    @Transactional
    public StampDtos.StampPolicyResponse update(UUID id, StampDtos.StampPolicyUpdateRequest request) {
        UUID tenantId = TenantContext.requireTenantId();
        StampPolicy p = stampPolicyRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> new IllegalArgumentException("스탬프 정책을 찾을 수 없습니다."));
        validateCouponTemplate(tenantId, request.couponTemplateId());

        if (request.active()) {
            deactivateOthers(tenantId, p.getId());
        }
        p.update(
                request.name().trim(),
                request.amountWonPerStamp(),
                request.stampsRequiredForCoupon(),
                request.couponTemplateId(),
                request.issuanceMode(),
                request.active()
        );
        stampPolicyRepository.save(p);
        return toResponse(p);
    }

    private void deactivateOthers(UUID tenantId, UUID keepId) {
        stampPolicyRepository.findByTenantId(tenantId).stream()
                .filter(x -> !x.getId().equals(keepId) && x.isActive())
                .forEach(x -> x.setActive(false));
    }

    private void validateCouponTemplate(UUID tenantId, UUID couponTemplateId) {
        CouponTemplate t = couponTemplateRepository.findByTenantIdAndId(tenantId, couponTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰 템플릿을 찾을 수 없습니다."));
        if (!t.isActive()) {
            throw new IllegalArgumentException("비활성 쿠폰 템플릿은 정책에 연결할 수 없습니다.");
        }
    }

    private StampDtos.StampPolicyResponse toResponse(StampPolicy p) {
        UUID tenantId = TenantContext.requireTenantId();
        String templateName = couponTemplateRepository.findByTenantIdAndId(tenantId, p.getCouponTemplateId())
                .map(CouponTemplate::getName)
                .orElse("-");
        return new StampDtos.StampPolicyResponse(
                p.getId(),
                p.getName(),
                p.getAmountWonPerStamp(),
                p.getStampsRequiredForCoupon(),
                p.getCouponTemplateId(),
                templateName,
                p.getIssuanceMode(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
