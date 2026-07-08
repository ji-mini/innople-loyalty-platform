package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.MemberGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberGradeHistoryRepository extends JpaRepository<MemberGradeHistory, UUID> {

    List<MemberGradeHistory> findByTenantIdAndMemberIdOrderByChangedAtDesc(UUID tenantId, UUID memberId);
}
