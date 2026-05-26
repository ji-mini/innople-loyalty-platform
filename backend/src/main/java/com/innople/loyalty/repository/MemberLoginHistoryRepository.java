package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.MemberLoginHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberLoginHistoryRepository extends JpaRepository<MemberLoginHistory, UUID> {
    List<MemberLoginHistory> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId, Pageable pageable);
}
