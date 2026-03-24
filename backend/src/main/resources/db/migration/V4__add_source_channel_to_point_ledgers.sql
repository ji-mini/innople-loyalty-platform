ALTER TABLE point_ledgers ADD COLUMN IF NOT EXISTS source_channel VARCHAR(40);

UPDATE point_ledgers
SET source_channel = CASE
    WHEN event_type IN ('EARN', 'ADJUST_EARN') THEN 'ADMIN_WEB_MANUAL_EARN'
    WHEN event_type IN ('USE', 'ADJUST_USE') THEN 'ADMIN_WEB_MANUAL_USE'
    WHEN event_type = 'EXPIRE_AUTO' THEN 'SYSTEM_AUTO_EXPIRE'
    ELSE 'ADMIN_WEB_MANUAL_EXPIRE'
END
WHERE source_channel IS NULL;

ALTER TABLE point_ledgers ALTER COLUMN source_channel SET NOT NULL;
