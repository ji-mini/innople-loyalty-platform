-- 어드민 계정 권한(role) 변경 이력 테이블.
-- 상태 변경 이력(admin_user_status_history)과 별도로, role 전이만 독립적으로 기록한다.
-- (승인 시점에 role이 함께 바뀌는 경우와, 이미 ACTIVE인 계정의 role만 단독으로 바뀌는 경우 모두 대상)
CREATE TABLE IF NOT EXISTS admin_user_role_history (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    admin_user_id UUID NOT NULL,
    changed_by UUID,
    from_role VARCHAR(30),
    to_role VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_admin_user_role_history_tenant_admin
    ON admin_user_role_history (tenant_id, admin_user_id, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_admin_user_role_history_tenant_changed_at
    ON admin_user_role_history (tenant_id, changed_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_admin_user_role_history_tenant') THEN
        ALTER TABLE admin_user_role_history
            ADD CONSTRAINT fk_admin_user_role_history_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_admin_user_role_history_admin_user') THEN
        ALTER TABLE admin_user_role_history
            ADD CONSTRAINT fk_admin_user_role_history_admin_user
                FOREIGN KEY (admin_user_id) REFERENCES admin_users (id);
    END IF;
END $$;
