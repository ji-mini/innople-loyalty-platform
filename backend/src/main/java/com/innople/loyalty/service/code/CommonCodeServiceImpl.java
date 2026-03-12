package com.innople.loyalty.service.code;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.code.CommonCode;
import com.innople.loyalty.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.innople.loyalty.service.code.CommonCodeExceptions.CommonCodeAlreadyExistsException;
import static com.innople.loyalty.service.code.CommonCodeExceptions.CommonCodeNotFoundException;

@Service
@RequiredArgsConstructor
public class CommonCodeServiceImpl implements CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommonCodeItem> list(String codeGroup, Boolean active, String keyword) {
        UUID tenantId = TenantContext.requireTenantId();
        String g = normalize(codeGroup);
        String k = normalize(keyword);
        return commonCodeRepository.search(tenantId, g, active, k).stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    @Transactional
    public CommonCodeItem create(String codeGroup, String code, String name, boolean active, int sortOrder) {
        UUID tenantId = TenantContext.requireTenantId();
        String g = requireTrimmed(codeGroup, "codeGroup");
        String c = requireTrimmed(code, "code");
        String n = requireTrimmed(name, "name");

        if (commonCodeRepository.existsByTenantIdAndCodeGroupAndCode(tenantId, g, c)) {
            throw new CommonCodeAlreadyExistsException("CommonCode already exists");
        }

        try {
            CommonCode saved = commonCodeRepository.save(CommonCode.of(g, c, n, active, sortOrder));
            return toItem(saved);
        } catch (DataIntegrityViolationException e) {
            throw new CommonCodeAlreadyExistsException("CommonCode already exists");
        }
    }

    @Override
    @Transactional
    public CommonCodeItem update(UUID commonCodeId, String name, boolean active, int sortOrder) {
        UUID tenantId = TenantContext.requireTenantId();
        CommonCode cc = commonCodeRepository.findByTenantIdAndId(tenantId, commonCodeId)
                .orElseThrow(() -> new CommonCodeNotFoundException("CommonCode not found"));
        cc.change(name, active, sortOrder);
        CommonCode saved = commonCodeRepository.save(cc);
        return toItem(saved);
    }

    private CommonCodeItem toItem(CommonCode c) {
        return new CommonCodeItem(
                c.getId(),
                c.getCodeGroup(),
                c.getCode(),
                c.getName(),
                c.isActive(),
                c.getSortOrder(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private String normalize(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isBlank() ? null : t;
    }

    private String requireTrimmed(String v, String fieldName) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return v.trim();
    }
}

