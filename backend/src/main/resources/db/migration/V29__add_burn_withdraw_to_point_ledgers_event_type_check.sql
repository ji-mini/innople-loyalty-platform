-- point_ledgers.event_type 는 @Enumerated(EnumType.STRING) 매핑이라, Hibernate 6 이
-- 컬럼 최초 생성 시점의 PointEventType 값들로 point_ledgers_event_type_check 를 자동 생성했다.
-- 이후 PointEventType 에 BURN_WITHDRAW(탈회 소각)가 추가됐으나 ddl-auto=update 는
-- 기존 CHECK 제약을 갱신하지 않으므로, 탈회 소각 원장(event_type='BURN_WITHDRAW') INSERT 가
-- 제약 위반(point_ledgers_event_type_check)으로 실패한다.
--
-- Hibernate 자동생성 제약을 제거하고, 프로젝트 컨벤션(ck_ 접두, 예: ck_point_ledgers_reference_pair)에
-- 맞춰 명시적으로 관리되는 제약으로 교체한다.
-- 허용 값은 기존 6개(EARN, USE, EXPIRE_AUTO, EXPIRE_MANUAL, ADJUST_EARN, ADJUST_USE) 전체 +
-- BURN_WITHDRAW 이며, 기존 데이터는 수정하지 않는다.

ALTER TABLE point_ledgers
    DROP CONSTRAINT IF EXISTS point_ledgers_event_type_check;

ALTER TABLE point_ledgers
    DROP CONSTRAINT IF EXISTS ck_point_ledgers_event_type;

ALTER TABLE point_ledgers
    ADD CONSTRAINT ck_point_ledgers_event_type
        CHECK (event_type IN (
            'EARN',
            'USE',
            'EXPIRE_AUTO',
            'EXPIRE_MANUAL',
            'ADJUST_EARN',
            'ADJUST_USE',
            'BURN_WITHDRAW'
        ));
