ALTER TABLE point_ledgers
    ADD COLUMN IF NOT EXISTS reference_type VARCHAR(50);

ALTER TABLE point_ledgers
    ADD COLUMN IF NOT EXISTS reference_id VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_point_ledgers_tenant_reference
    ON point_ledgers (tenant_id, reference_type, reference_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_point_ledgers_reference_pair'
    ) THEN
        ALTER TABLE point_ledgers
            ADD CONSTRAINT ck_point_ledgers_reference_pair
                CHECK (
                    (reference_type IS NULL AND reference_id IS NULL)
                    OR (reference_type IS NOT NULL AND reference_id IS NOT NULL)
                );
    END IF;
END $$;
