CREATE UNIQUE INDEX IF NOT EXISTS uk_point_ledgers_tenant_pos_reference
    ON point_ledgers (tenant_id, reference_type, reference_id)
    WHERE reference_type IN ('POS_EARN_TXN', 'POS_USE_TXN')
      AND reference_id IS NOT NULL
      AND reference_id <> '';
