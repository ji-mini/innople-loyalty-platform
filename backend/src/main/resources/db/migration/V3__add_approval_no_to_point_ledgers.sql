ALTER TABLE point_ledgers ADD COLUMN IF NOT EXISTS approval_no VARCHAR(12);

UPDATE point_ledgers
SET approval_no = UPPER(SUBSTRING(MD5(id::text) FROM 1 FOR 12))
WHERE approval_no IS NULL;

ALTER TABLE point_ledgers ALTER COLUMN approval_no SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_point_ledgers_tenant_approval_no
    ON point_ledgers (tenant_id, approval_no);

ALTER TABLE point_lots ADD COLUMN IF NOT EXISTS source_ledger_id UUID;

CREATE INDEX IF NOT EXISTS idx_point_lots_tenant_source_ledger
    ON point_lots (tenant_id, source_ledger_id);
