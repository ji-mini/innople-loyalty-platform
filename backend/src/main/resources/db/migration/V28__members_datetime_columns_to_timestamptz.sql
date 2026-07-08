-- members 의 일시 성격 컬럼 5종을 DATE → timestamptz(timestamp with time zone) 로 승격한다.
-- BaseEntity.createdAt/updatedAt(Instant) 와 매핑 타입을 Instant 로 통일하기 위함.
-- 기존 DATE 값은 자정 00:00 UTC 로 캐스팅된다(과거 데이터는 감수).
-- birth_date(생년월일)는 날짜만 의미하므로 DATE 를 그대로 유지한다.
ALTER TABLE members
  ALTER COLUMN joined_at             TYPE timestamptz USING joined_at::timestamptz,
  ALTER COLUMN dormant_at            TYPE timestamptz USING dormant_at::timestamptz,
  ALTER COLUMN suspended_at          TYPE timestamptz USING suspended_at::timestamptz,
  ALTER COLUMN withdraw_requested_at TYPE timestamptz USING withdraw_requested_at::timestamptz,
  ALTER COLUMN withdrawn_at          TYPE timestamptz USING withdrawn_at::timestamptz;
