package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.MemberStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberStatusHistoryRepository extends JpaRepository<MemberStatusHistory, UUID> {

    List<MemberStatusHistory> findByTenantIdAndMemberIdOrderByChangedAtDesc(UUID tenantId, UUID memberId);
}
