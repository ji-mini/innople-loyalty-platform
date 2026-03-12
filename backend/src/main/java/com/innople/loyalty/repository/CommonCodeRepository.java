package com.innople.loyalty.repository;

import com.innople.loyalty.domain.code.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommonCodeRepository extends JpaRepository<CommonCode, UUID> {
    Optional<CommonCode> findByTenantIdAndCodeGroupAndCodeAndActiveIsTrue(UUID tenantId, String codeGroup, String code);

    Optional<CommonCode> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndCodeGroupAndCode(UUID tenantId, String codeGroup, String code);

    @Query("""
            select c
            from CommonCode c
            where c.tenantId = :tenantId
              and (:codeGroup is null or :codeGroup = '' or c.codeGroup = :codeGroup)
              and (:active is null or c.active = :active)
              and (
                :keyword is null
                or :keyword = ''
                or lower(c.code) like lower(concat('%', :keyword, '%'))
                or lower(c.name) like lower(concat('%', :keyword, '%'))
              )
            order by c.codeGroup asc, c.sortOrder asc, c.code asc
            """)
    List<CommonCode> search(
            @Param("tenantId") UUID tenantId,
            @Param("codeGroup") String codeGroup,
            @Param("active") Boolean active,
            @Param("keyword") String keyword
    );
}

