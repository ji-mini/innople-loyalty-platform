-- 스탬프(포인트와 분리), 쿠폰 템플릿(스탬프 보상 연결용)

CREATE TABLE IF NOT EXISTS coupon_templates (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_coupon_templates_tenant_id ON coupon_templates (tenant_id);

-- 복합 FK (tenant_id, id) 참조용 — PostgreSQL 요구사항
CREATE UNIQUE INDEX IF NOT EXISTS uk_coupon_templates_tenant_id_id ON coupon_templates (tenant_id, id);

CREATE TABLE IF NOT EXISTS stamp_policies (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    amount_won_per_stamp BIGINT NOT NULL,
    stamps_required_for_coupon INT NOT NULL,
    coupon_template_id UUID NOT NULL,
    issuance_mode VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_stamp_policies_amount_positive CHECK (amount_won_per_stamp > 0),
    CONSTRAINT chk_stamp_policies_stamps_positive CHECK (stamps_required_for_coupon > 0),
    CONSTRAINT chk_stamp_policies_issuance_mode CHECK (issuance_mode IN ('AUTO', 'MANUAL'))
);

CREATE INDEX IF NOT EXISTS idx_stamp_policies_tenant_id ON stamp_policies (tenant_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_policies_tenant_id_id ON stamp_policies (tenant_id, id);

-- 테넌트당 활성 정책 1개
CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_policies_tenant_active
    ON stamp_policies (tenant_id)
    WHERE active = TRUE;

CREATE TABLE IF NOT EXISTS stamp_accounts (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL,
    current_balance INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_stamp_accounts_balance_non_negative CHECK (current_balance >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_accounts_tenant_member ON stamp_accounts (tenant_id, member_id);
CREATE INDEX IF NOT EXISTS idx_stamp_accounts_tenant_id ON stamp_accounts (tenant_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_accounts_tenant_id_id ON stamp_accounts (tenant_id, id);

CREATE TABLE IF NOT EXISTS stamp_ledgers (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    account_id UUID NOT NULL,
    member_id UUID NOT NULL,
    policy_id UUID,
    event_type VARCHAR(30) NOT NULL,
    stamp_delta INT NOT NULL,
    reason VARCHAR(500),
    reference_type VARCHAR(50),
    reference_id VARCHAR(100),
    purchase_amount_won BIGINT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_stamp_ledgers_tenant_member ON stamp_ledgers (tenant_id, member_id);
CREATE INDEX IF NOT EXISTS idx_stamp_ledgers_tenant_created ON stamp_ledgers (tenant_id, created_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_ledgers_tenant_id_id ON stamp_ledgers (tenant_id, id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_ledgers_pos_idempotent
    ON stamp_ledgers (tenant_id, reference_type, reference_id)
    WHERE reference_type = 'POS_ORDER' AND reference_id IS NOT NULL AND reference_id <> '';

CREATE TABLE IF NOT EXISTS stamp_coupon_issues (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL,
    stamp_policy_id UUID NOT NULL,
    coupon_template_id UUID NOT NULL,
    redemption_ledger_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_coupon_issues_tenant_redemption_ledger
    ON stamp_coupon_issues (tenant_id, redemption_ledger_id);

CREATE INDEX IF NOT EXISTS idx_stamp_coupon_issues_tenant_member ON stamp_coupon_issues (tenant_id, member_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_stamp_coupon_issues_tenant_id_id ON stamp_coupon_issues (tenant_id, id);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_coupon_templates_tenant') THEN
        ALTER TABLE coupon_templates
            ADD CONSTRAINT fk_coupon_templates_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_policies_tenant') THEN
        ALTER TABLE stamp_policies
            ADD CONSTRAINT fk_stamp_policies_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_policies_coupon_template_tenant_scoped') THEN
        ALTER TABLE stamp_policies
            ADD CONSTRAINT fk_stamp_policies_coupon_template_tenant_scoped
                FOREIGN KEY (tenant_id, coupon_template_id) REFERENCES coupon_templates (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_accounts_tenant') THEN
        ALTER TABLE stamp_accounts
            ADD CONSTRAINT fk_stamp_accounts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_accounts_member_tenant_scoped') THEN
        ALTER TABLE stamp_accounts
            ADD CONSTRAINT fk_stamp_accounts_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_ledgers_tenant') THEN
        ALTER TABLE stamp_ledgers
            ADD CONSTRAINT fk_stamp_ledgers_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_ledgers_account_tenant_scoped') THEN
        ALTER TABLE stamp_ledgers
            ADD CONSTRAINT fk_stamp_ledgers_account_tenant_scoped
                FOREIGN KEY (tenant_id, account_id) REFERENCES stamp_accounts (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_ledgers_member_tenant_scoped') THEN
        ALTER TABLE stamp_ledgers
            ADD CONSTRAINT fk_stamp_ledgers_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_ledgers_policy_tenant_scoped') THEN
        ALTER TABLE stamp_ledgers
            ADD CONSTRAINT fk_stamp_ledgers_policy_tenant_scoped
                FOREIGN KEY (tenant_id, policy_id) REFERENCES stamp_policies (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_coupon_issues_tenant') THEN
        ALTER TABLE stamp_coupon_issues
            ADD CONSTRAINT fk_stamp_coupon_issues_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_coupon_issues_member_tenant_scoped') THEN
        ALTER TABLE stamp_coupon_issues
            ADD CONSTRAINT fk_stamp_coupon_issues_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_coupon_issues_policy_tenant_scoped') THEN
        ALTER TABLE stamp_coupon_issues
            ADD CONSTRAINT fk_stamp_coupon_issues_policy_tenant_scoped
                FOREIGN KEY (tenant_id, stamp_policy_id) REFERENCES stamp_policies (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_coupon_issues_coupon_template_tenant_scoped') THEN
        ALTER TABLE stamp_coupon_issues
            ADD CONSTRAINT fk_stamp_coupon_issues_coupon_template_tenant_scoped
                FOREIGN KEY (tenant_id, coupon_template_id) REFERENCES coupon_templates (tenant_id, id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stamp_coupon_issues_redemption_ledger_tenant_scoped') THEN
        ALTER TABLE stamp_coupon_issues
            ADD CONSTRAINT fk_stamp_coupon_issues_redemption_ledger_tenant_scoped
                FOREIGN KEY (tenant_id, redemption_ledger_id) REFERENCES stamp_ledgers (tenant_id, id);
    END IF;
END $$;

COMMENT ON TABLE coupon_templates IS '스탬프 보상 등에서 참조하는 쿠폰 템플릿(테넌트별)';
COMMENT ON TABLE stamp_policies IS '스탬프 적립/보상 정책(테넌트당 활성 1개)';
COMMENT ON TABLE stamp_accounts IS '회원별 스탬프 잔액(캐시, 원장 합계와 일치)';
COMMENT ON TABLE stamp_ledgers IS '스탬프 적립/차감 원장(신뢰 소스)';
COMMENT ON TABLE stamp_coupon_issues IS '스탬프→쿠폰 전환 발급 기록(중복 방지)';
