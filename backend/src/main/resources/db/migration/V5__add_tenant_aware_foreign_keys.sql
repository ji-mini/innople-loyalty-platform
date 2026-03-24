CREATE UNIQUE INDEX IF NOT EXISTS uk_addresses_tenant_id_id
    ON addresses (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_membership_grades_tenant_id_id
    ON membership_grades (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_admin_users_tenant_id_id
    ON admin_users (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_members_tenant_id_id
    ON members (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_point_accounts_tenant_id_id
    ON point_accounts (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_point_ledgers_tenant_id_id
    ON point_ledgers (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_point_lots_tenant_id_id
    ON point_lots (tenant_id, id);

CREATE INDEX IF NOT EXISTS idx_members_tenant_address_id
    ON members (tenant_id, address_id);

CREATE INDEX IF NOT EXISTS idx_members_tenant_membership_grade_id
    ON members (tenant_id, membership_grade_id);

CREATE INDEX IF NOT EXISTS idx_api_audit_logs_tenant_admin_user_id
    ON api_audit_logs (tenant_id, admin_user_id);

CREATE INDEX IF NOT EXISTS idx_point_allocations_tenant_account_id
    ON point_allocations (tenant_id, account_id);

CREATE INDEX IF NOT EXISTS idx_point_lots_tenant_member_id
    ON point_lots (tenant_id, member_id);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_addresses_tenant') THEN
        ALTER TABLE addresses
            ADD CONSTRAINT fk_addresses_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_membership_grades_tenant') THEN
        ALTER TABLE membership_grades
            ADD CONSTRAINT fk_membership_grades_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_admin_users_tenant') THEN
        ALTER TABLE admin_users
            ADD CONSTRAINT fk_admin_users_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_common_codes_tenant') THEN
        ALTER TABLE common_codes
            ADD CONSTRAINT fk_common_codes_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_members_tenant') THEN
        ALTER TABLE members
            ADD CONSTRAINT fk_members_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_ledgers_tenant') THEN
        ALTER TABLE member_ledgers
            ADD CONSTRAINT fk_member_ledgers_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_accounts_tenant') THEN
        ALTER TABLE point_accounts
            ADD CONSTRAINT fk_point_accounts_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_ledgers_tenant') THEN
        ALTER TABLE point_ledgers
            ADD CONSTRAINT fk_point_ledgers_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_lots_tenant') THEN
        ALTER TABLE point_lots
            ADD CONSTRAINT fk_point_lots_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_allocations_tenant') THEN
        ALTER TABLE point_allocations
            ADD CONSTRAINT fk_point_allocations_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_policy_tenant') THEN
        ALTER TABLE point_policy
            ADD CONSTRAINT fk_point_policy_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_api_audit_logs_tenant') THEN
        ALTER TABLE api_audit_logs
            ADD CONSTRAINT fk_api_audit_logs_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_members_address_tenant_scoped') THEN
        ALTER TABLE members
            ADD CONSTRAINT fk_members_address_tenant_scoped
                FOREIGN KEY (tenant_id, address_id) REFERENCES addresses (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_members_membership_grade_tenant_scoped') THEN
        ALTER TABLE members
            ADD CONSTRAINT fk_members_membership_grade_tenant_scoped
                FOREIGN KEY (tenant_id, membership_grade_id) REFERENCES membership_grades (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_api_audit_logs_admin_user_tenant_scoped') THEN
        ALTER TABLE api_audit_logs
            ADD CONSTRAINT fk_api_audit_logs_admin_user_tenant_scoped
                FOREIGN KEY (tenant_id, admin_user_id) REFERENCES admin_users (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_accounts_member_tenant_scoped') THEN
        ALTER TABLE point_accounts
            ADD CONSTRAINT fk_point_accounts_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_ledgers_account_tenant_scoped') THEN
        ALTER TABLE point_ledgers
            ADD CONSTRAINT fk_point_ledgers_account_tenant_scoped
                FOREIGN KEY (tenant_id, account_id) REFERENCES point_accounts (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_ledgers_member_tenant_scoped') THEN
        ALTER TABLE point_ledgers
            ADD CONSTRAINT fk_point_ledgers_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_lots_account_tenant_scoped') THEN
        ALTER TABLE point_lots
            ADD CONSTRAINT fk_point_lots_account_tenant_scoped
                FOREIGN KEY (tenant_id, account_id) REFERENCES point_accounts (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_lots_member_tenant_scoped') THEN
        ALTER TABLE point_lots
            ADD CONSTRAINT fk_point_lots_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'point_lots'
          AND column_name = 'source_ledger_id'
    ) AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_lots_source_ledger_tenant_scoped') THEN
        ALTER TABLE point_lots
            ADD CONSTRAINT fk_point_lots_source_ledger_tenant_scoped
                FOREIGN KEY (tenant_id, source_ledger_id) REFERENCES point_ledgers (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_allocations_account_tenant_scoped') THEN
        ALTER TABLE point_allocations
            ADD CONSTRAINT fk_point_allocations_account_tenant_scoped
                FOREIGN KEY (tenant_id, account_id) REFERENCES point_accounts (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_allocations_ledger_tenant_scoped') THEN
        ALTER TABLE point_allocations
            ADD CONSTRAINT fk_point_allocations_ledger_tenant_scoped
                FOREIGN KEY (tenant_id, ledger_id) REFERENCES point_ledgers (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_point_allocations_lot_tenant_scoped') THEN
        ALTER TABLE point_allocations
            ADD CONSTRAINT fk_point_allocations_lot_tenant_scoped
                FOREIGN KEY (tenant_id, lot_id) REFERENCES point_lots (tenant_id, id);
    END IF;
END $$;
