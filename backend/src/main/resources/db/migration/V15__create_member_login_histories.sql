CREATE TABLE IF NOT EXISTS member_login_histories (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL,
    member_no VARCHAR(50) NOT NULL,
    login_id VARCHAR(50) NOT NULL,
    ip VARCHAR(45),
    user_agent VARCHAR(400),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_member_login_histories_tenant_created_at
    ON member_login_histories (tenant_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_member_login_histories_tenant_member_created_at
    ON member_login_histories (tenant_id, member_id, created_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_login_histories_tenant') THEN
        ALTER TABLE member_login_histories
            ADD CONSTRAINT fk_member_login_histories_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_login_histories_member_tenant_scoped') THEN
        ALTER TABLE member_login_histories
            ADD CONSTRAINT fk_member_login_histories_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;
END $$;
