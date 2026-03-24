package com.innople.loyalty.service.points;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PointPolicyService {
    List<PointPolicyItem> list();

    PointPolicyItem create(String pointType, String name, int validityDays, boolean enabled, String description);

    PointPolicyItem update(UUID policyId, String pointType, String name, int validityDays, boolean enabled, String description);

    record PointPolicyItem(
            UUID id,
            String pointType,
            String name,
            int validityDays,
            boolean enabled,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

