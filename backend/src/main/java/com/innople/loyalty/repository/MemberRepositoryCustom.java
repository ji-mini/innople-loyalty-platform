package com.innople.loyalty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/**
 * {@link MemberRepository} 의 커스텀 조회 확장.
 *
 * <p>가입일(joinedAt) 범위 필터는 값이 있을 때만 predicate 를 조립해야 하므로 정적 {@code @Query} 대신
 * 동적 JPQL 로 구현한다. {@code (:date is null or joined_at >= :date)} 패턴은 파라미터가 null 일 때
 * PostgreSQL 이 timestamptz 파라미터의 타입을 추론하지 못해
 * "could not determine data type of parameter" 오류를 유발하기 때문이다.</p>
 */
public interface MemberRepositoryCustom {

    Page<MemberRepository.MemberSummaryView> searchSummary(
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
    );
}
