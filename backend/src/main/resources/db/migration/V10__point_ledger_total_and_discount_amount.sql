-- POS 연동 시 총 구매금액·할인금액(원, 선택)
ALTER TABLE point_ledgers
    ADD COLUMN IF NOT EXISTS total_purchase_amount BIGINT NULL,
    ADD COLUMN IF NOT EXISTS discount_amount BIGINT NULL;

COMMENT ON COLUMN point_ledgers.total_purchase_amount IS '총 구매금액(원), POS 연동 시';
COMMENT ON COLUMN point_ledgers.discount_amount IS '할인금액(원), POS 연동 시';
