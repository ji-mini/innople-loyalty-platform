package com.innople.loyalty.service.member;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.member.Gender;
import com.innople.loyalty.domain.member.Member;
import com.innople.loyalty.domain.member.MemberLedgerEventType;
import com.innople.loyalty.domain.member.MemberStatusCodes;
import com.innople.loyalty.repository.AddressRepository;
import com.innople.loyalty.repository.CommonCodeRepository;
import com.innople.loyalty.repository.MemberRepository;
import com.innople.loyalty.repository.MembershipGradeRepository;
import com.innople.loyalty.service.memberauth.MemberCredentialService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    private final UUID tenantId = UUID.randomUUID();

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private MemberLedgerService memberLedgerService;

    @Mock
    private MembershipGradeRepository membershipGradeRepository;

    @Mock
    private CommonCodeRepository commonCodeRepository;

    @Mock
    private MemberCredentialService memberCredentialService;

    @InjectMocks
    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void updateMyProfileRecordsUpdateInfoLedger() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.register(
                "MABC1234567",
                "기존이름",
                LocalDate.of(1990, 1, 1),
                null,
                Gender.UNKNOWN,
                "01011112222",
                "before@example.com",
                null,
                null,
                null,
                MemberStatusCodes.ACTIVE,
                LocalDate.of(2026, 1, 1),
                null,
                null,
                null,
                null
        );
        setEntityId(member, memberId);

        when(memberRepository.findByTenantIdAndId(tenantId, memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);
        when(memberCredentialService.findByMemberId(memberId)).thenReturn(Optional.empty());

        MemberResult result = memberService.updateMyProfile(memberId, new MemberService.UpdateInfoCommand(
                "변경이름",
                LocalDate.of(1991, 2, 2),
                null,
                Gender.FEMALE,
                "01033334444",
                "after@example.com",
                null,
                "updated-web-id",
                "updated-ci",
                "birthday"
        ));

        verify(memberLedgerService).record(member, MemberLedgerEventType.UPDATE_INFO, MemberStatusCodes.ACTIVE, MemberStatusCodes.ACTIVE);
        assertEquals("변경이름", result.name());
        assertEquals("after@example.com", result.email());
        assertEquals("01033334444", result.phoneNumber());
    }

    private static void setEntityId(Member member, UUID memberId) {
        try {
            java.lang.reflect.Field idField = com.innople.loyalty.domain.common.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(member, memberId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
