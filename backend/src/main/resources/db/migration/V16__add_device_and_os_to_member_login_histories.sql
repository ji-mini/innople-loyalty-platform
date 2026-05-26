ALTER TABLE member_login_histories
    ADD COLUMN IF NOT EXISTS device_name VARCHAR(80),
    ADD COLUMN IF NOT EXISTS os_name VARCHAR(80);
