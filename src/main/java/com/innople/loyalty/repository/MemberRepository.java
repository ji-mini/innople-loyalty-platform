package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Member> findByTenantIdAndMemberNo(UUID tenantId, String memberNo);

    boolean existsByTenantIdAndMemberNo(UUID tenantId, String memberNo);
}

