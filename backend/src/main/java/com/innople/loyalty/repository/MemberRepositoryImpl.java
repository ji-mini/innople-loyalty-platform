package com.innople.loyalty.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * {@link MemberRepositoryCustom} 구현체. Spring Data 규약에 따라 {@code MemberRepository} + {@code Impl}
 * 이름으로 자동 결합된다.
 */
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<MemberRepository.MemberSummaryView> searchSummary(
            UUID tenantId,
            String keyword,
            String statusCode,
            String memberNo,
            String phoneNumber,
            String name,
            String webId,
            Instant joinedFrom,
            Instant joinedTo,
            Pageable pageable
    ) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhere(tenantId, keyword, statusCode, memberNo, phoneNumber, name, webId, joinedFrom, joinedTo, params);

        String selectJpql = """
                select new com.innople.loyalty.repository.MemberSummaryRow(
                  m.id,
                  m.memberNo,
                  m.name,
                  m.statusCode,
                  g.id,
                  g.name,
                  m.phoneNumber,
                  m.email,
                  m.webId,
                  m.joinedAt,
                  m.dormantAt,
                  m.withdrawnAt,
                  coalesce(pa.currentBalance, 0),
                  (select case when count(mc.id) > 0 then true else false end
                     from MemberCredential mc
                    where mc.tenantId = m.tenantId
                      and mc.memberId = m.id
                      and mc.deleted = false)
                )
                from Member m
                left join m.membershipGrade g on g.tenantId = m.tenantId
                left join PointAccount pa
                  on pa.tenantId = m.tenantId
                 and pa.memberId = m.id
                """ + where + buildOrderBy(pageable.getSort());

        TypedQuery<MemberSummaryRow> query = entityManager.createQuery(selectJpql, MemberSummaryRow.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<MemberRepository.MemberSummaryView> content = new ArrayList<>(query.getResultList());

        String countJpql = "select count(m) from Member m" + where;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        params.forEach(countQuery::setParameter);
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private String buildWhere(
            UUID tenantId,
            String keyword,
            String statusCode,
            String memberNo,
            String phoneNumber,
            String name,
            String webId,
            Instant joinedFrom,
            Instant joinedTo,
            Map<String, Object> params
    ) {
        StringBuilder where = new StringBuilder("""
                 where m.tenantId = :tenantId
                  and (:statusCode is null or m.statusCode = :statusCode)
                  and (:memberNo is null or lower(m.memberNo) like lower(concat('%', cast(:memberNo as string), '%')))
                  and (:phoneNumber is null or lower(m.phoneNumber) like lower(concat('%', cast(:phoneNumber as string), '%')))
                  and (:name is null or lower(m.name) like lower(concat('%', cast(:name as string), '%')))
                  and (:webId is null or lower(m.webId) like lower(concat('%', cast(:webId as string), '%')))
                  and (
                        :keyword is null
                     or lower(m.memberNo) like lower(concat('%', cast(:keyword as string), '%'))
                     or lower(m.name) like lower(concat('%', cast(:keyword as string), '%'))
                     or lower(m.phoneNumber) like lower(concat('%', cast(:keyword as string), '%'))
                     or lower(m.email) like lower(concat('%', cast(:keyword as string), '%'))
                     or lower(m.webId) like lower(concat('%', cast(:keyword as string), '%'))
                  )
                """);

        params.put("tenantId", tenantId);
        params.put("statusCode", statusCode);
        params.put("memberNo", memberNo);
        params.put("phoneNumber", phoneNumber);
        params.put("name", name);
        params.put("webId", webId);
        params.put("keyword", keyword);

        // 가입일 범위는 값이 있을 때만 조건을 추가한다. null 파라미터를 바인딩하지 않아
        // timestamptz 파라미터 타입 미결정(could not determine data type) 오류를 원천 차단한다.
        // 경계는 컨트롤러에서 DateRangeUtils 로 계산한 KST 반열린 구간 [joinedFrom, joinedTo) 을 그대로 사용한다.
        if (joinedFrom != null) {
            where.append("  and m.joinedAt >= :joinedFrom\n");
            params.put("joinedFrom", joinedFrom);
        }
        if (joinedTo != null) {
            where.append("  and m.joinedAt < :joinedTo\n");
            params.put("joinedTo", joinedTo);
        }

        return where.toString();
    }

    // ORDER BY 는 데이터 쿼리에만 붙는다. property 를 문자열로 조립하므로 JPQL 인젝션 방지를 위해
    // 허용된 정렬 컬럼만 통과시킨다. 유효한 정렬 항목이 하나도 없으면 기본 정렬로 fallback 하여
    // ORDER BY 가 절대 비지 않도록 보장한다(빈 ORDER BY 는 페이징 결과를 비결정적으로 만든다).
    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
            "joinedAt", "createdAt", "memberNo", "name", "statusCode");

    private static final String DEFAULT_ORDER_BY = " order by m.joinedAt desc, m.createdAt desc";

    private String buildOrderBy(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return DEFAULT_ORDER_BY;
        }
        StringJoiner joiner = new StringJoiner(", ", " order by ", "");
        boolean hasValidOrder = false;
        for (Sort.Order order : sort) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                continue;
            }
            joiner.add("m." + order.getProperty() + (order.isAscending() ? " asc" : " desc"));
            hasValidOrder = true;
        }
        return hasValidOrder ? joiner.toString() : DEFAULT_ORDER_BY;
    }
}
