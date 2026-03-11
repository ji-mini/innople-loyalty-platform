package com.innople.loyalty.service.points;

import com.innople.loyalty.domain.points.PointPolicyType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PointPolicyService {
    List<PointPolicyItem> list();

    PointPolicyItem create(PointPolicyType pointType, String name, int validityDays, boolean enabled, String description);

    PointPolicyItem update(UUID policyId, PointPolicyType pointType, String name, int validityDays, boolean enabled, String description);

    record PointPolicyItem(
            UUID id,
            PointPolicyType pointType,
            String name,
            int validityDays,
            boolean enabled,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

