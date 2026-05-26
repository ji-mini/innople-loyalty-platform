package com.innople.loyalty.service.tenant;

import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantValidationServiceImpl implements TenantValidationService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional(readOnly = true)
    public void requireExistingTenant(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId가 없습니다.");
        }
        tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 tenantId입니다."));
    }
}
