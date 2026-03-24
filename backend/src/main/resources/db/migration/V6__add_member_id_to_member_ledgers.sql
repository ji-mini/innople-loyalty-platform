ALTER TABLE member_ledgers
    ADD COLUMN IF NOT EXISTS member_id UUID;

UPDATE member_ledgers ml
SET member_id = m.id
FROM members m
WHERE ml.member_id IS NULL
  AND ml.tenant_id = m.tenant_id
  AND ml.member_no = m.member_no;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM member_ledgers
        WHERE member_id IS NULL
    ) THEN
        RAISE EXCEPTION 'member_ledgers.member_id backfill failed for some rows';
    END IF;
END $$;

ALTER TABLE member_ledgers
    ALTER COLUMN member_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_member_ledgers_tenant_member_id
    ON member_ledgers (tenant_id, member_id);

DROP INDEX IF EXISTS idx_member_ledgers_tenant_member_no;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_ledgers_member_tenant_scoped') THEN
        ALTER TABLE member_ledgers
            ADD CONSTRAINT fk_member_ledgers_member_tenant_scoped
                FOREIGN KEY (tenant_id, member_id) REFERENCES members (tenant_id, id);
    END IF;
END $$;
