ALTER TABLE member_credentials
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30);

ALTER TABLE member_credentials
    ALTER COLUMN email DROP NOT NULL;

UPDATE member_credentials mc
SET phone_number = m.phone_number
FROM members m
WHERE mc.tenant_id = m.tenant_id
  AND mc.member_id = m.id
  AND mc.phone_number IS NULL;

DROP INDEX IF EXISTS uk_member_credentials_tenant_email;

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_credentials_tenant_phone_active
    ON member_credentials (tenant_id, phone_number)
    WHERE deleted = false AND phone_number IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_member_credentials_tenant_phone
    ON member_credentials (tenant_id, phone_number);
