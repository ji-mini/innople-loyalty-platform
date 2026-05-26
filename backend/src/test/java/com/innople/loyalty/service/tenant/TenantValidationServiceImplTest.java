package com.innople.loyalty.service.tenant;

import com.innople.loyalty.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantValidationServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantValidationServiceImpl tenantValidationService;

    @Test
    void passesWhenTenantExists() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.of(org.mockito.Mockito.mock(com.innople.loyalty.domain.tenant.Tenant.class)));

        assertDoesNotThrow(() -> tenantValidationService.requireExistingTenant(tenantId));
    }

    @Test
    void throwsBadRequestWhenTenantDoesNotExist() {
        UUID tenantId = UUID.randomUUID();
        when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tenantValidationService.requireExistingTenant(tenantId)
        );

        assertEquals("존재하지 않는 tenantId입니다.", exception.getMessage());
    }
}
