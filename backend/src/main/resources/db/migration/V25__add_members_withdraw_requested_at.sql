-- 탈퇴요청(WITHDRAW_REQUESTED) 유예기간 관리를 위한 탈퇴요청일시 컬럼 추가.
ALTER TABLE members ADD COLUMN IF NOT EXISTS withdraw_requested_at DATE;
