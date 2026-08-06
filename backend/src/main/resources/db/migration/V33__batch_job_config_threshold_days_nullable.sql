-- V33: batch_job_config.threshold_days nullable 완화
-- 포인트 자동소멸(POINT_EXPIRATION) 등 threshold 가 무의미한 배치를 지원한다.
-- enum CHECK 등 익명 제약은 만들지 않는다(named 제약만 유지).

-- 1) 기존 named CHECK 제거 후 nullable + 완화된 named CHECK 재생성
ALTER TABLE batch_job_config
    DROP CONSTRAINT IF EXISTS ck_batch_job_config_threshold_days;

ALTER TABLE batch_job_config
    ALTER COLUMN threshold_days DROP NOT NULL;

ALTER TABLE batch_job_config
    ADD CONSTRAINT ck_batch_job_config_threshold_days
        CHECK (threshold_days IS NULL OR threshold_days > 0);
