CREATE TABLE IF NOT EXISTS member_credentials (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_credentials_tenant_member
    ON member_credentials (tenant_id, member_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_credentials_tenant_email
    ON member_credentials (tenant_id, email);

CREATE INDEX IF NOT EXISTS idx_member_credentials_tenant_id
    ON member_credentials (tenant_id);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_credentials_member_tenant_scoped') THEN
        ALTER TABLE member_credentials
            ADD CONSTRAINT fk_member_credentials_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;
END $$;
