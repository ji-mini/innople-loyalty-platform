-- 어드민 계정 상태에 REJECTED(거절) 추가.
-- status 컬럼은 VARCHAR(30)이며 별도 enum 타입이 아니므로 CHECK 제약으로 허용 값을 강제한다.
-- 기존 데이터는 변환하지 않는다(INACTIVE로 잘못 들어간 거절 케이스가 있더라도 자동 변환하지 않음).
ALTER TABLE admin_users
    DROP CONSTRAINT IF EXISTS ck_admin_users_status;

ALTER TABLE admin_users
    ADD CONSTRAINT ck_admin_users_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE', 'REJECTED'));
