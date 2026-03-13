package com.innople.loyalty.config;

import com.innople.loyalty.domain.tenant.Tenant;
import com.innople.loyalty.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TenantRepresentativeCodeBackfillRunner implements ApplicationRunner {

    private final TenantRepository tenantRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Tenant> tenants = tenantRepository.findAll();
        if (tenants.isEmpty()) return;

        Set<String> used = new HashSet<>();
        for (Tenant t : tenants) {
            String code = t.getRepresentativeCode();
            if (code != null && !code.isBlank()) {
                used.add(code.trim().toUpperCase(Locale.ROOT));
            }
        }

        for (Tenant t : tenants) {
            if (t.getRepresentativeCode() != null && !t.getRepresentativeCode().isBlank()) continue;
            String generated = generateUniqueCode(t, used);
            t.changeRepresentativeCode(generated);
            used.add(generated);
        }
    }

    private String generateUniqueCode(Tenant t, Set<String> used) {
        // UUID 기반으로 안정적으로 2자리 코드를 생성(재기동해도 값이 바뀌지 않도록)
        String base = t.getTenantId().toString().replace("-", "").toUpperCase(Locale.ROOT);
        for (int i = 0; i + 1 < base.length(); i += 2) {
            char a = toLetter(base.charAt(i));
            char b = toLetter(base.charAt(i + 1));
            String candidate = "" + a + b;
            if (!used.contains(candidate)) return candidate;
        }
        // 최후: 이름 기반으로 생성
        String name = (t.getName() == null ? "TENANT" : t.getName()).toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (name.length() >= 2) {
            String candidate = name.substring(0, 2);
            if (!used.contains(candidate)) return candidate;
        }
        // 마지막까지 충돌 시 순차 탐색
        for (char a = 'A'; a <= 'Z'; a++) {
            for (char b = 'A'; b <= 'Z'; b++) {
                String candidate = "" + a + b;
                if (!used.contains(candidate)) return candidate;
            }
        }
        throw new IllegalStateException("cannot generate representativeCode");
    }

    private char toLetter(char hex) {
        int v = Character.digit(hex, 16);
        if (v < 0) v = 0;
        return (char) ('A' + (v % 26));
    }
}

