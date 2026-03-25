package com.innople.loyalty.config;

import com.innople.loyalty.domain.code.CommonCode;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommonCodeSeedRunner implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Tenant> tenants = tenantRepository.findAll();
        for (Tenant t : tenants) {
            TenantContext.setTenantId(t.getTenantId());
            try {
                seedAdminRole();
                seedMemberStatus();
                seedPointReason();
            } finally {
                TenantContext.clear();
            }
        }
    }

    private void seedAdminRole() {
        upsert("ADMIN_ROLE", "SUPER_ADMIN", "슈퍼관리자", true, 10);
        upsert("ADMIN_ROLE", "ADMIN", "관리자", true, 20);
        // Domain enum is OPERATOR, but UI label is "사용자"
        upsert("ADMIN_ROLE", "OPERATOR", "사용자", true, 30);
    }

    private void seedMemberStatus() {
        upsert(MemberStatusCodes.GROUP, MemberStatusCodes.ACTIVE, "정상", true, 10);
        upsert(MemberStatusCodes.GROUP, MemberStatusCodes.DORMANT, "휴면", true, 20);
        upsert(MemberStatusCodes.GROUP, MemberStatusCodes.SUSPENDED, "정지", true, 30);
        upsert(MemberStatusCodes.GROUP, MemberStatusCodes.WITHDRAWN, "탈퇴", true, 40);

        // legacy
        upsert(MemberStatusCodes.GROUP, MemberStatusCodes.LEGACY_NORMAL, "정상(레거시)", true, 11);
        upsert(MemberStatusCodes.GROUP, MemberStatusCodes.LEGACY_WITHDRAW_REQUESTED, "탈퇴요청(레거시)", true, 35);
    }

    private void seedPointReason() {
        upsert("POINT_REASON", "CS_COMP", "고객보상", true, 10);
        upsert("POINT_REASON", "ADJ_FIX", "오등록정정", true, 20);
        upsert("POINT_REASON", "EVENT", "이벤트", true, 30);
        upsert("POINT_REASON", "PURCHASE", "구매", true, 40);
        upsert("POINT_REASON", "SIGNUP", "신규가입", true, 50);
    }

    private void upsert(String codeGroup, String code, String name, boolean active, int sortOrder) {
        if (commonCodeRepository.existsByTenantIdAndCodeGroupAndCode(TenantContext.requireTenantId(), codeGroup, code)) {
            return;
        }
        commonCodeRepository.save(CommonCode.of(codeGroup, code, name, active, sortOrder));
    }
}

