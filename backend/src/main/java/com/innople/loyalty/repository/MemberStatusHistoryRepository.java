package com.innople.loyalty.repository;

import com.innople.loyalty.controller.dto.MemberQueryDtos;
import com.innople.loyalty.domain.member.MemberStatusHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberStatusHistoryRepository extends JpaRepository<MemberStatusHistory, UUID> {

    List<MemberStatusHistory> findByTenantIdAndMemberIdOrderByChangedAtDesc(UUID tenantId, UUID memberId);

    /**
     * 관리자 화면용 상태 변경 이력 조회.
     * <p>★LEFT JOIN 필수★ changed_by 가 NULL 인 행(actor_type = SYSTEM/MEMBER)도 감사 로그로 반드시 반환해야 하므로
     * inner join(콤마 조인)을 쓰지 않는다. admin_users 는 FK 가 없고 hard delete 가 가능하므로 이름이 없을 수 있으며
     * 그 경우 changedByName 은 null 이 된다.
     * <p>★크로스 테넌트 방지★ 조인 조건에 tenantId 일치를 반드시 포함한다.
     */
    @Query("""
            select new com.innople.loyalty.controller.dto.MemberQueryDtos$MemberStatusHistoryResponse(
                h.id,
                h.actorType,
                h.changedBy,
                au.name,
                h.reason,
                h.changedAt,
                h.fromStatus,
                h.toStatus
            )
            from MemberStatusHistory h
            left join com.innople.loyalty.domain.user.AdminUser au
                on au.id = h.changedBy and au.tenantId = h.tenantId
            where h.tenantId = :tenantId and h.memberId = :memberId
            order by h.changedAt desc
            """)
    List<MemberQueryDtos.MemberStatusHistoryResponse> findStatusHistoryView(
            @Param("tenantId") UUID tenantId,
            @Param("memberId") UUID memberId,
            Pageable pageable
    );
}
