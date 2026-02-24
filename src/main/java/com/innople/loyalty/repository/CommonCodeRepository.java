package com.innople.loyalty.repository;

import com.innople.loyalty.domain.code.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommonCodeRepository extends JpaRepository<CommonCode, UUID> {
    Optional<CommonCode> findByTenantIdAndCodeGroupAndCodeAndActiveIsTrue(UUID tenantId, String codeGroup, String code);
}

