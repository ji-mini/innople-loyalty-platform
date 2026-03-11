package com.innople.loyalty.repository;

import com.innople.loyalty.domain.user.AdminUser;
import com.innople.loyalty.domain.user.AdminRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {
    Optional<AdminUser> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<AdminUser> findByTenantIdAndEmail(UUID tenantId, String email);
    Optional<AdminUser> findByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);

    boolean existsByTenantIdAndRole(UUID tenantId, AdminRole role);

    @Query("""
            select a
            from AdminUser a
            where a.tenantId = :tenantId
              and (
                :keyword is null
                or :keyword = ''
                or lower(a.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(a.email, '')) like lower(concat('%', :keyword, '%'))
                or a.phoneNumber like concat('%', :keyword, '%')
              )
            order by a.updatedAt desc
            """)
    List<AdminUser> searchByTenantId(@Param("tenantId") UUID tenantId, @Param("keyword") String keyword);
}

