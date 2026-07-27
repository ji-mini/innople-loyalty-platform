package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID>, MemberRepositoryCustom {
    Optional<Member> findByTenantIdAndId(UUID tenantId, UUID id);

    @Query("""
            select m from Member m
            left join fetch m.membershipGrade
            where m.tenantId = :tenantId and m.id = :id
            """)
    Optional<Member> findByTenantIdAndIdWithMembershipGrade(@Param("tenantId") UUID tenantId, @Param("id") UUID id);

    @EntityGraph(attributePaths = {"address", "membershipGrade"})
    Optional<Member> findByTenantIdAndMemberNo(UUID tenantId, String memberNo);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndJoinedAt(UUID tenantId, Instant joinedAt);

    long countByTenantIdAndCreatedAtBetween(UUID tenantId, Instant from, Instant to);

    // KST half-open 구간 집계: joinedAt >= start AND joinedAt < endExclusive
    long countByTenantIdAndJoinedAtGreaterThanEqualAndJoinedAtLessThan(UUID tenantId, Instant start, Instant endExclusive);

    long countByTenantIdAndJoinedAtLessThanEqual(UUID tenantId, Instant to);

    long countByTenantIdAndStatusCode(UUID tenantId, String statusCode);

    long countByTenantIdAndDormantAtGreaterThanEqualAndDormantAtLessThan(UUID tenantId, Instant start, Instant endExclusive);

    long countByTenantIdAndWithdrawnAtGreaterThanEqualAndWithdrawnAtLessThan(UUID tenantId, Instant start, Instant endExclusive);

    @Query("select count(m) from Member m where m.tenantId = :tenantId and m.statusCode <> :excludeStatus")
    long countByTenantIdAndStatusCodeNot(@Param("tenantId") UUID tenantId, @Param("excludeStatus") String excludeStatus);

    // asOfEndExclusive = 기준일 다음 날 KST 자정. "기준일 끝까지 활성" = 그 이전에 가입했고, 그 이전에 탈퇴하지 않은 회원.
    @Query("""
            select count(m) from Member m
            where m.tenantId = :tenantId
              and m.joinedAt < :asOfEndExclusive
              and (m.withdrawnAt is null or m.withdrawnAt >= :asOfEndExclusive)
            """)
    long countActiveMembersAsOf(@Param("tenantId") UUID tenantId, @Param("asOfEndExclusive") Instant asOfEndExclusive);

    List<Member> findByTenantIdAndIdIn(UUID tenantId, List<UUID> ids);

    boolean existsByTenantIdAndMemberNo(UUID tenantId, String memberNo);
    boolean existsByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);
    boolean existsByTenantIdAndWebId(UUID tenantId, String webId);
    boolean existsByTenantIdAndEmail(UUID tenantId, String email);
    Optional<Member> findByTenantIdAndEmail(UUID tenantId, String email);

    @Query("""
            select max(m.memberNo)
            from Member m
            where m.tenantId = :tenantId
              and m.memberNo like concat(:prefix, '%')
            """)
    String findMaxMemberNoByTenantIdAndPrefix(
            @Param("tenantId") UUID tenantId,
            @Param("prefix") String prefix
    );

    interface MemberSummaryView {
        UUID getId();

        String getMemberNo();

        String getName();

        String getStatusCode();

        UUID getGradeId();

        String getGradeName();

        String getPhoneNumber();

        String getEmail();

        String getWebId();

        Instant getJoinedAt();

        Instant getDormantAt();

        Instant getWithdrawnAt();

        long getPointBalance();

        boolean getAppLoginEnabled();
    }

    @Query("""
            select m
            from Member m
            where m.tenantId = :tenantId
              and (:statusCode is null or m.statusCode = :statusCode)
              and (
                    :keyword is null
                 or lower(m.memberNo) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.name) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.phoneNumber) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.email) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.webId) like lower(concat('%', cast(:keyword as string), '%'))
              )
            """)
    Page<Member> search(
            @Param("tenantId") UUID tenantId,
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            Pageable pageable
    );

    // 회원 목록 조회(가입일 범위 필터 포함)는 MemberRepositoryCustom / MemberRepositoryImpl 에서
    // 동적 JPQL 로 구현한다. 가입일 필터를 (:date is null or ...) 로 처리하면 값이 없을 때
    // timestamptz 파라미터 타입 미결정 오류가 발생하기 때문이다.
}

