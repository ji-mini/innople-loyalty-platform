package com.innople.loyalty.service.points;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.points.PointPolicy;
import com.innople.loyalty.domain.points.PointPolicyType;
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

    private final PointPolicyRepository pointPolicyRepository;

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
    public PointPolicyItem create(PointPolicyType pointType, String name, int validityDays, boolean enabled, String description) {
        UUID tenantId = TenantContext.requireTenantId();
        if (pointPolicyRepository.existsByTenantIdAndPointType(tenantId, pointType)) {
            throw new PointPolicyAlreadyExistsException("PointPolicy already exists for pointType=" + pointType);
        }
        PointPolicy saved = pointPolicyRepository.save(new PointPolicy(pointType, name, validityDays, enabled, description));
        return toItem(saved);
    }

    @Override
    @Transactional
    public PointPolicyItem update(UUID policyId, PointPolicyType pointType, String name, int validityDays, boolean enabled, String description) {
        UUID tenantId = TenantContext.requireTenantId();
        PointPolicy policy = pointPolicyRepository.findByTenantIdAndId(tenantId, policyId)
                .orElseThrow(() -> new PointPolicyNotFoundException("PointPolicy not found"));
        policy.change(pointType, name, validityDays, enabled, description);
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
}

