package com.innople.loyalty.service.member;

import java.util.UUID;

public interface MembershipGradeService {

    MembershipGradeItem create(String name, int level, String description);

    MembershipGradeItem update(UUID id, String name, int level, String description);

    void delete(UUID id);

    record MembershipGradeItem(
            UUID id,
            String code,
            String name,
            String description
    ) {
    }
}
