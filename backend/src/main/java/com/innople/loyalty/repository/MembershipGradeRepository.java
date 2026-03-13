package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.MembershipGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipGradeRepository extends JpaRepository<MembershipGrade, UUID> {
    List<MembershipGrade> findAllByTenantIdOrderByLevelAsc(UUID tenantId);

    Optional<MembershipGrade> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<MembershipGrade> findByTenantIdAndLevel(UUID tenantId, int level);
}

