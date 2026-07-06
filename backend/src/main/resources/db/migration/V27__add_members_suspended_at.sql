-- 정지(SUSPENDED) 상태의 정지일시 컬럼 추가. 휴면(dormant_at)과 대칭 구조.
ALTER TABLE members ADD COLUMN IF NOT EXISTS suspended_at DATE;
