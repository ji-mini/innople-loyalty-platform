package com.innople.loyalty.repository;

import com.innople.loyalty.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByTenantIdAndId(UUID tenantId, UUID id);
    Optional<Member> findByTenantIdAndMemberNo(UUID tenantId, String memberNo);

    boolean existsByTenantIdAndMemberNo(UUID tenantId, String memberNo);

    interface MemberSummaryView {
        UUID getId();

        String getMemberNo();

        String getName();

        String getStatusCode();

        String getPhoneNumber();

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
                 or lower(m.memberNo) like lower(concat('%', :keyword, '%'))
                 or lower(m.name) like lower(concat('%', :keyword, '%'))
                 or lower(m.phoneNumber) like lower(concat('%', :keyword, '%'))
                 or lower(m.webId) like lower(concat('%', :keyword, '%'))
                 or lower(m.ci) like lower(concat('%', :keyword, '%'))
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
              and (:memberNo is null or lower(m.memberNo) like lower(concat('%', :memberNo, '%')))
              and (:phoneNumber is null or lower(m.phoneNumber) like lower(concat('%', :phoneNumber, '%')))
              and (:name is null or lower(m.name) like lower(concat('%', :name, '%')))
              and (:webId is null or lower(m.webId) like lower(concat('%', :webId, '%')))
              and (:joinedFrom is null or m.joinedAt >= :joinedFrom)
              and (:joinedTo is null or m.joinedAt <= :joinedTo)
              and (
                    :keyword is null
                 or lower(m.memberNo) like lower(concat('%', :keyword, '%'))
                 or lower(m.name) like lower(concat('%', :keyword, '%'))
                 or lower(m.phoneNumber) like lower(concat('%', :keyword, '%'))
                 or lower(m.webId) like lower(concat('%', :keyword, '%'))
                 or lower(m.ci) like lower(concat('%', :keyword, '%'))
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

