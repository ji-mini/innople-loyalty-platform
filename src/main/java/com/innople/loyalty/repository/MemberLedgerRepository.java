package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.MemberLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MemberLedgerRepository extends JpaRepository<MemberLedger, UUID> {
    List<MemberLedger> findTop100ByTenantIdAndMemberNoOrderByCreatedAtDesc(UUID tenantId, String memberNo);
}

