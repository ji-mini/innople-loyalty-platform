package com.innople.loyalty.service.tenant;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.common.TenantMismatchException;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.innople.loyalty.service.tenant.TenantAdminExceptions.TenantDeleteConflictException;
import static com.innople.loyalty.service.tenant.TenantAdminExceptions.TenantNotFoundException;

@Service
@RequiredArgsConstructor
public class TenantAdminServiceImpl implements TenantAdminService {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public TenantDetail create(String name, String representativeCode) {
        Tenant saved = tenantRepository.save(new Tenant(name, representativeCode));
        return toDetail(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantDetail get(UUID tenantId) {
        UUID ctx = TenantContext.requireTenantId();
        if (!ctx.equals(tenantId)) {
            throw new TenantMismatchException("tenantId mismatch with TenantContext");
        }
        Tenant t = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("tenant not found"));
        return toDetail(t);
    }

    @Override
    @Transactional
    public TenantDetail update(UUID tenantId, String name, String representativeCode) {
        UUID ctx = TenantContext.getTenantId().orElse(null);
        if (ctx != null && !ctx.equals(tenantId)) {
            throw new TenantMismatchException("tenantId mismatch with TenantContext");
        }
        Tenant t = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("tenant not found"));
        t.changeName(name);
        t.changeRepresentativeCode(representativeCode);
        Tenant saved = tenantRepository.save(t);
        return toDetail(saved);
    }

    @Override
    @Transactional
    public void delete(UUID tenantId) {
        Tenant tenant = tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("tenant not found"));
        try {
            tenantRepository.delete(tenant);
            tenantRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new TenantDeleteConflictException("연관 데이터가 있는 테넌트는 삭제할 수 없습니다.");
        }
    }

    private TenantDetail toDetail(Tenant t) {
        return new TenantDetail(t.getTenantId(), t.getName(), t.getRepresentativeCode(), t.getCreatedAt(), t.getUpdatedAt());
    }
}

