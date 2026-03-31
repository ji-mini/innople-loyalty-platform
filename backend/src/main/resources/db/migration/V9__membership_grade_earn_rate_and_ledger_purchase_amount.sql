-- 등급별 적립률(%), 포인트 원장의 적립 대상 금액(원, POS 연동 시)
ALTER TABLE membership_grades
    ADD COLUMN IF NOT EXISTS earn_rate_percent NUMERIC(5, 2) NOT NULL DEFAULT 0;

ALTER TABLE point_ledgers
    ADD COLUMN IF NOT EXISTS purchase_amount BIGINT NULL;

COMMENT ON COLUMN membership_grades.earn_rate_percent IS '적립 대상 금액 대비 적립률(%) — POS 적립 시 회원 등급으로 포인트 계산';
COMMENT ON COLUMN point_ledgers.purchase_amount IS '적립 시 기준 적립 대상 금액(원), 수기 적립 등에서는 NULL';
