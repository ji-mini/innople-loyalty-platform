package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByTenantIdAndId(UUID tenantId, UUID id);

    @EntityGraph(attributePaths = {"address", "membershipGrade"})
    Optional<Member> findByTenantIdAndMemberNo(UUID tenantId, String memberNo);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndJoinedAt(UUID tenantId, LocalDate joinedAt);

    long countByTenantIdAndJoinedAtBetween(UUID tenantId, LocalDate from, LocalDate to);

    long countByTenantIdAndJoinedAtLessThanEqual(UUID tenantId, LocalDate to);

    long countByTenantIdAndStatusCode(UUID tenantId, String statusCode);

    long countByTenantIdAndDormantAtBetween(UUID tenantId, LocalDate from, LocalDate to);

    long countByTenantIdAndWithdrawnAtBetween(UUID tenantId, LocalDate from, LocalDate to);

    @Query("select count(m) from Member m where m.tenantId = :tenantId and m.statusCode <> :excludeStatus")
    long countByTenantIdAndStatusCodeNot(@Param("tenantId") UUID tenantId, @Param("excludeStatus") String excludeStatus);

    @Query("""
            select count(m) from Member m
            where m.tenantId = :tenantId
              and m.joinedAt <= :asOfDate
              and (m.withdrawnAt is null or m.withdrawnAt > :asOfDate)
            """)
    long countActiveMembersAsOf(@Param("tenantId") UUID tenantId, @Param("asOfDate") LocalDate asOfDate);

    List<Member> findByTenantIdAndIdIn(UUID tenantId, List<UUID> ids);

    boolean existsByTenantIdAndMemberNo(UUID tenantId, String memberNo);
    boolean existsByTenantIdAndPhoneNumber(UUID tenantId, String phoneNumber);
    boolean existsByTenantIdAndWebId(UUID tenantId, String webId);

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

        String getPhoneNumber();

        String getEmail();

        String getWebId();

        LocalDate getJoinedAt();

        LocalDate getDormantAt();

        LocalDate getWithdrawnAt();

        long getPointBalance();
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

    @Query("""
            select
              m.id as id,
              m.memberNo as memberNo,
              m.name as name,
              m.statusCode as statusCode,
              m.phoneNumber as phoneNumber,
              m.email as email,
              m.webId as webId,
              m.joinedAt as joinedAt,
              m.dormantAt as dormantAt,
              m.withdrawnAt as withdrawnAt,
              coalesce(pa.currentBalance, 0) as pointBalance
            from Member m
            left join PointAccount pa
              on pa.tenantId = m.tenantId
             and pa.memberId = m.id
            where m.tenantId = :tenantId
              and (:statusCode is null or m.statusCode = :statusCode)
              and (:memberNo is null or lower(m.memberNo) like lower(concat('%', cast(:memberNo as string), '%')))
              and (:phoneNumber is null or lower(m.phoneNumber) like lower(concat('%', cast(:phoneNumber as string), '%')))
              and (:name is null or lower(m.name) like lower(concat('%', cast(:name as string), '%')))
              and (:webId is null or lower(m.webId) like lower(concat('%', cast(:webId as string), '%')))
              and (:joinedFrom is null or m.joinedAt >= :joinedFrom)
              and (:joinedTo is null or m.joinedAt <= :joinedTo)
              and (
                    :keyword is null
                 or lower(m.memberNo) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.name) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.phoneNumber) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.email) like lower(concat('%', cast(:keyword as string), '%'))
                 or lower(m.webId) like lower(concat('%', cast(:keyword as string), '%'))
              )
            """)
    Page<MemberSummaryView> searchSummary(
            @Param("tenantId") UUID tenantId,
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            @Param("memberNo") String memberNo,
            @Param("phoneNumber") String phoneNumber,
            @Param("name") String name,
            @Param("webId") String webId,
            @Param("joinedFrom") LocalDate joinedFrom,
            @Param("joinedTo") LocalDate joinedTo,
            Pageable pageable
    );
}

