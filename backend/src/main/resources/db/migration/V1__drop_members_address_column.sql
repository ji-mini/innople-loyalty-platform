-- members 테이블에 address_id가 있으므로 기존 address 컬럼 제거
ALTER TABLE members DROP COLUMN IF EXISTS address;
