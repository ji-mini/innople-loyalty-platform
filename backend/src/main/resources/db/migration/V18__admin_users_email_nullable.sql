-- admin_users.email은 선택(optional) 입력값이다.
-- 실제 로그인/식별 ID 역할은 phone_number가 담당하므로 email의 NOT NULL 제약을 제거한다.
-- (이미 nullable인 경우에도 안전하게 no-op 처리된다.)
ALTER TABLE admin_users
    ALTER COLUMN email DROP NOT NULL;

-- 실제 식별 역할인 (tenant_id, phone_number) 조합의 UNIQUE 제약을 보장한다(없을 때만 생성).
CREATE UNIQUE INDEX IF NOT EXISTS idx_admin_users_tenant_phone_number
    ON admin_users (tenant_id, phone_number);

-- email은 선택값이지만 입력된 경우 테넌트 내 중복을 막기 위한 UNIQUE 제약을 유지한다.
-- (PostgreSQL은 NULL을 서로 다른 값으로 취급하므로 email 미입력 계정이 여러 개 있어도 충돌하지 않는다.)
CREATE UNIQUE INDEX IF NOT EXISTS idx_admin_users_tenant_email
    ON admin_users (tenant_id, email);
