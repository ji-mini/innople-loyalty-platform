package com.innople.loyalty.service.code;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.code.CommonCode;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.CommonCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommonCodeSeedServiceImpl implements CommonCodeSeedService {

    private final CommonCodeRepository commonCodeRepository;

    @Override
    @Transactional
    public SeedResult seedDefaultsForCurrentTenant() {
        UUID tenantId = TenantContext.requireTenantId();
        int created = 0;

        created += upsertIfMissing(tenantId, "ADMIN_ROLE", "SUPER_ADMIN", "슈퍼관리자", true, 10);
        created += upsertIfMissing(tenantId, "ADMIN_ROLE", "ADMIN", "관리자", true, 20);
        created += upsertIfMissing(tenantId, "ADMIN_ROLE", "OPERATOR", "사용자", true, 30);

        created += upsertIfMissing(tenantId, MemberStatusCodes.GROUP, MemberStatusCodes.ACTIVE, "정상", true, 10);
        created += upsertIfMissing(tenantId, MemberStatusCodes.GROUP, MemberStatusCodes.DORMANT, "휴면", true, 20);
        created += upsertIfMissing(tenantId, MemberStatusCodes.GROUP, MemberStatusCodes.SUSPENDED, "정지", true, 30);
        created += upsertIfMissing(tenantId, MemberStatusCodes.GROUP, MemberStatusCodes.WITHDRAWN, "탈퇴", true, 40);
        // legacy (호환)
        created += upsertIfMissing(tenantId, MemberStatusCodes.GROUP, MemberStatusCodes.LEGACY_NORMAL, "정상(레거시)", true, 11);
        created += upsertIfMissing(tenantId, MemberStatusCodes.GROUP, MemberStatusCodes.LEGACY_WITHDRAW_REQUESTED, "탈퇴요청(레거시)", true, 35);

        created += upsertIfMissing(tenantId, "POINT_REASON", "CS_COMP", "고객보상", true, 10);
        created += upsertIfMissing(tenantId, "POINT_REASON", "ADJ_FIX", "오등록정정", true, 20);
        created += upsertIfMissing(tenantId, "POINT_REASON", "EVENT", "이벤트", true, 30);
        created += upsertIfMissing(tenantId, "POINT_REASON", "PURCHASE", "구매", true, 40);
        created += upsertIfMissing(tenantId, "POINT_REASON", "SIGNUP", "신규가입", true, 50);

        return new SeedResult(created);
    }

    private int upsertIfMissing(UUID tenantId, String codeGroup, String code, String name, boolean active, int sortOrder) {
        if (commonCodeRepository.existsByTenantIdAndCodeGroupAndCode(tenantId, codeGroup, code)) {
            return 0;
        }
        commonCodeRepository.save(CommonCode.of(codeGroup, code, name, active, sortOrder));
        return 1;
    }
}

