package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Member> findByTenantIdAndMemberNo(UUID tenantId, String memberNo);

    boolean existsByTenantIdAndMemberNo(UUID tenantId, String memberNo);

    @Query("""
            select m
            from Member m
            where m.tenantId = :tenantId
              and (:statusCode is null or m.statusCode = :statusCode)
              and (
                    :keyword is null
                 or lower(m.memberNo) like lower(concat('%', :keyword, '%'))
                 or lower(m.name) like lower(concat('%', :keyword, '%'))
                 or lower(m.phoneNumber) like lower(concat('%', :keyword, '%'))
                 or lower(m.webId) like lower(concat('%', :keyword, '%'))
                 or lower(m.ci) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Member> search(
            @Param("tenantId") UUID tenantId,
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            Pageable pageable
    );
}

