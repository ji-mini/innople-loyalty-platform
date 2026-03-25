DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'point_policy_point_type_check'
    ) THEN
        ALTER TABLE point_policy
            DROP CONSTRAINT point_policy_point_type_check;
    END IF;
END $$;
