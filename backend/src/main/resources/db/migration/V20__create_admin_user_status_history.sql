-- 어드민 계정 상태 변경 이력 테이블.
CREATE TABLE IF NOT EXISTS admin_user_status_history (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    admin_user_id UUID NOT NULL,
    changed_by UUID,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_admin_user_status_history_tenant_admin
    ON admin_user_status_history (tenant_id, admin_user_id, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_admin_user_status_history_tenant_changed_at
    ON admin_user_status_history (tenant_id, changed_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_admin_user_status_history_tenant') THEN
        ALTER TABLE admin_user_status_history
            ADD CONSTRAINT fk_admin_user_status_history_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_admin_user_status_history_admin_user') THEN
        ALTER TABLE admin_user_status_history
            ADD CONSTRAINT fk_admin_user_status_history_admin_user
                FOREIGN KEY (admin_user_id) REFERENCES admin_users (id);
    END IF;
END $$;
