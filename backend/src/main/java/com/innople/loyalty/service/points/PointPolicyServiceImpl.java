package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.domain.points.PointPolicy;
import com.innople.loyalty.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.innople.loyalty.service.points.PointPolicyExceptions.PointPolicyAlreadyExistsException;
import static com.innople.loyalty.service.points.PointPolicyExceptions.PointPolicyNotFoundException;

@Service
@RequiredArgsConstructor
public class PointPolicyServiceImpl implements PointPolicyService {

    private static final String POINT_REASON_GROUP = "POINT_REASON";

    private final PointPolicyRepository pointPolicyRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PointPolicyItem> list() {
        UUID tenantId = TenantContext.requireTenantId();
        return pointPolicyRepository.findAllByTenantIdOrderByUpdatedAtDesc(tenantId)
                .stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    @Transactional
    public PointPolicyItem create(String pointType, String name, int validityDays, boolean enabled, String description) {
        UUID tenantId = TenantContext.requireTenantId();
        String normalizedPointType = validatePointType(tenantId, pointType);
        if (pointPolicyRepository.existsByTenantIdAndPointType(tenantId, normalizedPointType)) {
            throw new PointPolicyAlreadyExistsException("PointPolicy already exists for pointType=" + normalizedPointType);
        }
        PointPolicy saved = pointPolicyRepository.save(new PointPolicy(normalizedPointType, name, validityDays, enabled, description));
        return toItem(saved);
    }

    @Override
    @Transactional
    public PointPolicyItem update(UUID policyId, String pointType, String name, int validityDays, boolean enabled, String description) {
        UUID tenantId = TenantContext.requireTenantId();
        String normalizedPointType = validatePointType(tenantId, pointType);
        PointPolicy policy = pointPolicyRepository.findByTenantIdAndId(tenantId, policyId)
                .orElseThrow(() -> new PointPolicyNotFoundException("PointPolicy not found"));
        if (pointPolicyRepository.existsByTenantIdAndPointTypeAndIdNot(tenantId, normalizedPointType, policyId)) {
            throw new PointPolicyAlreadyExistsException("PointPolicy already exists for pointType=" + normalizedPointType);
        }
        policy.change(normalizedPointType, name, validityDays, enabled, description);
        PointPolicy saved = pointPolicyRepository.save(policy);
        return toItem(saved);
    }

    private PointPolicyItem toItem(PointPolicy p) {
        return new PointPolicyItem(
                p.getId(),
                p.getPointType(),
                p.getName(),
                p.getValidityDays(),
                p.isEnabled(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private String validatePointType(UUID tenantId, String pointType) {
        String normalized = (pointType == null) ? null : pointType.trim();
        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("pointType must not be blank");
        }
        commonCodeRepository.findByTenantIdAndCodeGroupAndCodeAndActiveIsTrue(tenantId, POINT_REASON_GROUP, normalized)
                .orElseThrow(() -> new IllegalArgumentException("Invalid pointType: " + normalized));
        return normalized;
    }
}

