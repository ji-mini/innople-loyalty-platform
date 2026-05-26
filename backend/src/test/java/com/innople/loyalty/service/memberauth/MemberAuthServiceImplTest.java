package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.config.auth.MemberJwtTokenProvider;
import com.innople.loyalty.domain.common.BaseEntity;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberCredential;
import com.innople.loyalty.domain.member.Gender;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.domain.points.PointAccount;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.MembershipGradeRepository;
import com.innople.loyalty.repository.PointAccountRepository;
import com.innople.loyalty.service.member.MemberLedgerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.innople.loyalty.domain.member.MemberStatusCodes.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceImplTest {

    private final UUID tenantId = UUID.randomUUID();

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberCredentialService memberCredentialService;

    @Mock
    private MembershipGradeRepository membershipGradeRepository;

    @Mock
    private PointAccountRepository pointAccountRepository;

    @Mock
    private MemberJwtTokenProvider memberJwtTokenProvider;

    @Mock
    private MemberLedgerService memberLedgerService;

    @Mock
    private MemberLoginHistoryService memberLoginHistoryService;

    @InjectMocks
    private MemberAuthServiceImpl memberAuthService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void signupRecordsRegisterLedgerForAppMember() throws Exception {
        UUID memberId = UUID.randomUUID();

        when(memberRepository.existsByTenantIdAndPhoneNumber(tenantId, "01062437691")).thenReturn(false);
        when(memberRepository.existsByTenantIdAndEmail(tenantId, "user@example.com")).thenReturn(false);
        when(membershipGradeRepository.findAllByTenantIdOrderByLevelAsc(tenantId)).thenReturn(List.of());
        when(memberRepository.existsByTenantIdAndMemberNo(eq(tenantId), anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            setField(BaseEntity.class, member, "id", memberId);
            setField(BaseEntity.class, member, "tenantId", tenantId);
            return member;
        });
        when(memberCredentialService.provision(memberId, "01062437691", "user@example.com", "password123"))
                .thenReturn(new MemberCredentialService.CredentialInfo(true, "01062437691"));
        when(pointAccountRepository.save(any(PointAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberJwtTokenProvider.createAccessToken(tenantId, memberId, "01062437691")).thenReturn("access-token");

        memberAuthService.signup(new MemberSignupCommand(
                "홍길동",
                "user@example.com",
                "password123",
                "010-6243-7691"
        ));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberLedgerService).record(memberCaptor.capture(), eq(MemberLedgerEventType.REGISTER), eq(ACTIVE), eq(ACTIVE));
        assertEquals(memberId, memberCaptor.getValue().getId());
        assertEquals("01062437691", memberCaptor.getValue().getPhoneNumber());
    }

    @Test
    void loginRecordsSuccessHistory() throws Exception {
        UUID memberId = UUID.randomUUID();
        Member member = Member.register(
                "MLOGIN12345",
                "홍길동",
                LocalDate.of(1990, 1, 1),
                null,
                Gender.UNKNOWN,
                "01062437691",
                "user@example.com",
                null,
                null,
                null,
                ACTIVE,
                LocalDate.of(2026, 1, 1),
                null,
                null,
                null,
                null
        );
        setField(BaseEntity.class, member, "id", memberId);
        setField(BaseEntity.class, member, "tenantId", tenantId);

        String passwordHash = org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
                "password123",
                org.springframework.security.crypto.bcrypt.BCrypt.gensalt()
        );
        MemberCredential credential = MemberCredential.of(memberId, "01062437691", "user@example.com", passwordHash);

        when(memberCredentialService.findByPhoneNumber("01062437691")).thenReturn(java.util.Optional.of(credential));
        when(memberCredentialService.findActiveByPhoneNumber("01062437691")).thenReturn(java.util.Optional.of(credential));
        when(memberRepository.findByTenantIdAndIdWithMembershipGrade(tenantId, memberId)).thenReturn(java.util.Optional.of(member));
        when(pointAccountRepository.findByTenantIdAndMemberId(tenantId, memberId)).thenReturn(java.util.Optional.empty());
        when(memberJwtTokenProvider.createAccessToken(tenantId, memberId, "01062437691")).thenReturn("access-token");

        MemberAuthResult result = memberAuthService.login(
                new MemberLoginCommand("010-6243-7691", "password123"),
                new MemberLoginContext("Android Phone", "Android 14", "127.0.0.1", "Mozilla/5.0")
        );

        verify(memberLoginHistoryService).recordSuccess(
                member,
                "01062437691",
                "Android Phone",
                "Android 14",
                "127.0.0.1",
                "Mozilla/5.0"
        );
        assertEquals("access-token", result.accessToken());
        assertEquals(memberId, result.memberId());
    }

    private static void setField(Class<?> type, Object target, String fieldName, Object value) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
