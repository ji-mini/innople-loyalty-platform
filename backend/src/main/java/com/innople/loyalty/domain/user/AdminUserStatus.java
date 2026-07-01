package com.innople.loyalty.domain.user;

/**
 * 어드민 계정 상태.
 * - PENDING: 가입 후 SUPER_ADMIN 승인 대기 (로그인 불가)
 * - ACTIVE: 승인 완료 (로그인 가능)
 * - INACTIVE: 활성 계정의 비활성화(정지) (로그인 불가, 재활성화 가능)
 * - REJECTED: 승인 대기 계정의 거절 (로그인 불가, 재승인 불가)
 */
public enum AdminUserStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    REJECTED
}
