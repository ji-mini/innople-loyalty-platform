package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.MemberLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MemberLedgerRepository extends JpaRepository<MemberLedger, UUID> {
    List<MemberLedger> findTop100ByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId);

    List<MemberLedger> findByTenantIdAndMemberIdOrderByCreatedAtDesc(UUID tenantId, UUID memberId, Pageable pageable);
}

