package com.innople.loyalty.domain.member;

/**
 * 이력(등급/상태 변경) 기록의 변경 주체 구분.
 * <ul>
 *     <li>{@code ADMIN}: 관리자 수기 변경. changed_by(관리자 id)가 반드시 채워진다.</li>
 *     <li>{@code SYSTEM}: 배치/시스템 자동 변경. changed_by 없이 기록될 수 있다.</li>
 *     <li>{@code MEMBER}: 회원 셀프 변경(예: 셀프 탈퇴 신청). changed_by 없이 기록된다.
 *         상태(status) 이력에만 허용되며, 등급(grade) 이력에는 허용하지 않는다(의도된 비대칭).</li>
 * </ul>
 */
public enum HistoryActorType {
    ADMIN,
    SYSTEM,
    MEMBER
}
