package com.innople.loyalty.config.dev;

import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevTenantDataInitializer implements ApplicationRunner {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (tenantRepository.count() > 0) {
            return;
        }

        tenantRepository.saveAll(List.of(
                new Tenant("INNOPLE 데모 테넌트 A", "AA"),
                new Tenant("INNOPLE 데모 테넌트 B", "BB"),
                new Tenant("INNOPLE 데모 테넌트 C", "CC")
        ));
    }
}

