package com.innople.loyalty.service.code;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommonCodeService {

    List<CommonCodeItem> list(String codeGroup, Boolean active, String keyword);

    CommonCodeItem create(String codeGroup, String code, String name, boolean active, int sortOrder);

    CommonCodeItem update(UUID commonCodeId, String name, boolean active, int sortOrder);

    record CommonCodeItem(
            UUID id,
            String codeGroup,
            String code,
            String name,
            boolean active,
            int sortOrder,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}

