package com.innople.loyalty.repository;

import com.innople.loyalty.controller.dto.MemberQueryDtos;
import com.innople.loyalty.domain.member.MemberGradeHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MemberGradeHistoryRepository extends JpaRepository<MemberGradeHistory, UUID> {

    List<MemberGradeHistory> findByTenantIdAndMemberIdOrderByChangedAtDesc(UUID tenantId, UUID memberId);

    /**
     * 관리자 화면용 등급 변경 이력 조회.
     * <p>★LEFT JOIN 필수★ changed_by 가 NULL 인 행(actor_type = SYSTEM)도 반드시 반환해야 하므로 inner join 을 쓰지 않는다.
     * from_grade_id 도 최초 부여 시 null 일 수 있으므로 등급 조인 역시 LEFT JOIN 이어야 한다.
     * admin_users 는 FK 가 없고 hard delete 가 가능하므로 changedByName 은 null 이 될 수 있다.
     * <p>★크로스 테넌트 방지★ 모든 조인 조건에 tenantId 일치를 포함한다.
     */
    @Query("""
            select new com.innople.loyalty.controller.dto.MemberQueryDtos$MemberGradeHistoryResponse(
                h.id,
                h.actorType,
                h.changedBy,
                au.name,
                h.reason,
                h.changedAt,
                h.fromGradeId,
                fg.name,
                h.toGradeId,
                tg.name
            )
            from MemberGradeHistory h
            left join com.innople.loyalty.domain.member.MembershipGrade fg
                on fg.id = h.fromGradeId and fg.tenantId = h.tenantId
            left join com.innople.loyalty.domain.member.MembershipGrade tg
                on tg.id = h.toGradeId and tg.tenantId = h.tenantId
            left join com.innople.loyalty.domain.user.AdminUser au
                on au.id = h.changedBy and au.tenantId = h.tenantId
            where h.tenantId = :tenantId and h.memberId = :memberId
            order by h.changedAt desc
            """)
    List<MemberQueryDtos.MemberGradeHistoryResponse> findGradeHistoryView(
            @Param("tenantId") UUID tenantId,
            @Param("memberId") UUID memberId,
            Pageable pageable
    );
}
