-- 어드민 계정 승인 워크플로우: status 컬럼 추가
-- PENDING(승인대기) / ACTIVE(활성) / INACTIVE(비활성)
ALTER TABLE admin_users
    ADD COLUMN IF NOT EXISTS status VARCHAR(30);

-- 기존 계정은 모두 활성(ACTIVE)으로 처리하여 로그인을 유지한다.
UPDATE admin_users SET status = 'ACTIVE' WHERE status IS NULL;

ALTER TABLE admin_users
    ALTER COLUMN status SET NOT NULL;
