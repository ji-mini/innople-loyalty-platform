package com.innople.loyalty.service.memberauth;

import com.innople.loyalty.config.TenantContext;
import com.innople.loyalty.domain.member.MemberCredential;
import com.innople.loyalty.repository.MemberCredentialRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberCredentialServiceImplTest {

    private final UUID tenantId = UUID.randomUUID();

    @Mock
    private MemberCredentialRepository memberCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberCredentialServiceImpl memberCredentialService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void provisionCreatesCredentialWhenActivePhoneDoesNotExist() {
        UUID memberId = UUID.randomUUID();
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        when(memberCredentialRepository.findByTenantIdAndPhoneNumberAndDeletedFalse(tenantId, "01012345678"))
                .thenReturn(Optional.empty());
        when(memberCredentialRepository.findByTenantIdAndMemberId(tenantId, memberId))
                .thenReturn(Optional.empty());
        when(memberCredentialRepository.save(any(MemberCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberCredentialService.CredentialInfo result = memberCredentialService.provision(
                memberId,
                "010-1234-5678",
                "user@example.com",
                "password123"
        );

        ArgumentCaptor<MemberCredential> captor = ArgumentCaptor.forClass(MemberCredential.class);
        verify(memberCredentialRepository).save(captor.capture());
        assertEquals("01012345678", captor.getValue().getPhoneNumber());
        assertEquals("01012345678", result.loginId());
    }

    @Test
    void provisionRejectsDuplicateActivePhoneNumberInSameTenant() {
        UUID memberId = UUID.randomUUID();
        UUID otherMemberId = UUID.randomUUID();
        MemberCredential existing = MemberCredential.of(otherMemberId, "01012345678", "other@example.com", "hash");

        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        when(memberCredentialRepository.findByTenantIdAndPhoneNumberAndDeletedFalse(tenantId, "01012345678"))
                .thenReturn(Optional.of(existing));

        assertThrows(
                MemberAuthExceptions.MemberCredentialAlreadyExistsException.class,
                () -> memberCredentialService.provision(memberId, "01012345678", "user@example.com", "password123")
        );
    }

    @Test
    void disableMarksCredentialAsDeleted() {
        UUID memberId = UUID.randomUUID();
        MemberCredential existing = MemberCredential.of(memberId, "01012345678", "user@example.com", "hash");
        when(memberCredentialRepository.findByTenantIdAndMemberId(tenantId, memberId))
                .thenReturn(Optional.of(existing));
        when(memberCredentialRepository.save(any(MemberCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        memberCredentialService.disable(memberId);

        assertTrue(existing.isDeleted());
    }
}
