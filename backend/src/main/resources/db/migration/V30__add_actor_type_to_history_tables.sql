-- member_grade_history / member_status_history 에 변경 주체 구분 컬럼(actor_type)을 추가한다.
-- 기존 행은 전부 관리자 수기 변경이며 changed_by 가 채워져 있으므로(NULL 0건 확인) DEFAULT 'ADMIN' 으로
-- 백필한 뒤 즉시 DEFAULT 를 제거한다. 이 두 단계는 한 세트다
-- (신규 INSERT 는 애플리케이션이 actor_type 을 명시하도록 강제하기 위해 기본값을 남기지 않는다).
--
-- 값/정합성 CHECK 은 반드시 명시적 이름으로 관리한다: 이 프로젝트는 Hibernate 가 @Enumerated 컬럼에
-- 익명 CHECK 을 자동 생성하고 ddl-auto=update 가 이를 갱신하지 않아 point_ledgers / member_ledgers
-- 에서 드리프트 문제가 이미 발생했다(ck_ 접두 컨벤션 준수).

-- 1) actor_type 컬럼 추가 (DEFAULT 백필 → 즉시 DROP DEFAULT)
ALTER TABLE member_grade_history
    ADD COLUMN actor_type VARCHAR(20) NOT NULL DEFAULT 'ADMIN';
ALTER TABLE member_grade_history
    ALTER COLUMN actor_type DROP DEFAULT;

ALTER TABLE member_status_history
    ADD COLUMN actor_type VARCHAR(20) NOT NULL DEFAULT 'ADMIN';
ALTER TABLE member_status_history
    ALTER COLUMN actor_type DROP DEFAULT;

-- 2) 값 CHECK (허용 값 명시)
ALTER TABLE member_grade_history
    ADD CONSTRAINT ck_member_grade_history_actor_type
        CHECK (actor_type IN ('ADMIN', 'SYSTEM'));

ALTER TABLE member_status_history
    ADD CONSTRAINT ck_member_status_history_actor_type
        CHECK (actor_type IN ('ADMIN', 'SYSTEM'));

-- 3) 주체 정합성 CHECK: ADMIN 이면 changed_by 필수.
--    반대 방향(SYSTEM → changed_by IS NULL)은 의도적으로 넣지 않는다
--    (관리자가 배치를 수동 트리거하는 경우 SYSTEM + changed_by 채움을 허용하기 위함).
ALTER TABLE member_grade_history
    ADD CONSTRAINT ck_member_grade_history_actor_consistency
        CHECK (actor_type <> 'ADMIN' OR changed_by IS NOT NULL);

ALTER TABLE member_status_history
    ADD CONSTRAINT ck_member_status_history_actor_consistency
        CHECK (actor_type <> 'ADMIN' OR changed_by IS NOT NULL);
