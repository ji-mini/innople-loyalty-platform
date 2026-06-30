package com.innople.loyalty.service.admin;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.config.auth.AdminJwtTokenProvider;
import com.innople.loyalty.domain.user.AdminRole;
import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.repository.AdminUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 직원(어드민) 계정 등록 시 email을 입력하지 않아도(빈 문자열/null)
 * email이 null로 정규화되어 저장되는지 검증한다.
 * (DB의 NOT NULL 제약은 Flyway V18 마이그레이션에서 제거되며,
 *  PostgreSQL은 NULL을 UNIQUE 충돌로 보지 않으므로 다건 미입력도 허용된다.)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminUserEmailOptionalTest {

    private final UUID tenantId = UUID.randomUUID();

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminJwtTokenProvider adminJwtTokenProvider;

    private AdminUserManagementServiceImpl managementService;
    private AdminAuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
        managementService = new AdminUserManagementServiceImpl(adminUserRepository, passwordEncoder);
        authService = new AdminAuthServiceImpl(adminUserRepository, passwordEncoder, adminJwtTokenProvider);

        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void create_withEmptyEmail_savesNullEmail() {
        managementService.create("010-1234-5678", "", "홍길동", "pw1234", AdminRole.OPERATOR);

        AdminUser saved = captureSavedAdminUser();
        assertNull(saved.getEmail(), "빈 문자열 email은 null로 저장되어야 한다");
    }

    @Test
    void create_withNullEmail_savesNullEmail() {
        managementService.create("01099998888", null, "김영희", "pw1234", AdminRole.OPERATOR);

        AdminUser saved = captureSavedAdminUser();
        assertNull(saved.getEmail(), "null email은 그대로 null로 저장되어야 한다");
    }

    @Test
    void register_withBlankEmail_savesNullEmail() {
        when(adminUserRepository.findByTenantIdAndPhoneNumber(eq(tenantId), any()))
                .thenReturn(Optional.empty());

        authService.register("010-2222-3333", "   ", "이철수", "pw1234");

        AdminUser saved = captureSavedAdminUser();
        assertNull(saved.getEmail(), "공백 email은 null로 저장되어야 한다");
    }

    private AdminUser captureSavedAdminUser() {
        ArgumentCaptor<AdminUser> captor = ArgumentCaptor.forClass(AdminUser.class);
        org.mockito.Mockito.verify(adminUserRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }
}
