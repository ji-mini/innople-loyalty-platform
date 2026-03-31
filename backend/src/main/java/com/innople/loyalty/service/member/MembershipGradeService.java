package com.innople.loyalty.service.member;

import java.math.BigDecimal;
import java.util.UUID;

public interface MembershipGradeService {

    MembershipGradeItem create(String name, int level, String description, BigDecimal earnRatePercent);

    MembershipGradeItem update(UUID id, String name, int level, String description, BigDecimal earnRatePercent);

    void delete(UUID id);

    record MembershipGradeItem(
            UUID id,
            String code,
            String name,
            String description,
            BigDecimal earnRatePercent
    ) {
    }
}
