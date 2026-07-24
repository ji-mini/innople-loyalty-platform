-- 회원 셀프 탈퇴 "신청"(ACTIVE -> WITHDRAW_REQUESTED) 등 회원 주체의 상태 변경 이력을 기록하기 위해
-- member_status_history 의 값 CHECK 에 'MEMBER' 를 추가한다.
--
-- Postgres 는 CHECK 을 in-place 로 수정할 수 없으므로 DROP → ADD 를 한 세트로 수행한다.
-- 제약 이름은 V30 과 동일하게 유지하여 Hibernate 익명 CHECK 자동 생성/드리프트를 방지한다.
--
-- ★비대칭 의도★ member_grade_history 의 값 CHECK 은 갱신하지 않는다.
--   회원이 자기 등급을 직접 변경하는 것은 정당한 시나리오가 아니므로 ADMIN/SYSTEM 으로 좁게 유지한다.
-- ※ 정합성 CHECK(ck_{table}_actor_consistency)은 건드리지 않는다.
--   MEMBER 는 changed_by NULL 로 (actor_type <> 'ADMIN' OR changed_by IS NOT NULL) 을 이미 통과한다.

ALTER TABLE member_status_history
    DROP CONSTRAINT ck_member_status_history_actor_type;

ALTER TABLE member_status_history
    ADD CONSTRAINT ck_member_status_history_actor_type
        CHECK (actor_type IN ('ADMIN', 'SYSTEM', 'MEMBER'));
