-- V34: 기존 전 테넌트에 POINT_EXPIRATION batch_job_config seed
-- 포인트 만료 배치 프레임워크 편입 후 config 부재로 스케줄 skip 되지 않도록 한다.
-- 값: enabled=true, run_hour=0 (KST 자정), threshold_days=NULL
-- 이미 행이 있으면 넣지 않는다 (멱등).
-- 전제: V33 (threshold_days nullable) 적용 완료.

INSERT INTO batch_job_config (
    id,
    tenant_id,
    batch_name,
    enabled,
    run_hour,
    threshold_days,
    created_at,
    updated_at,
    updated_by
)
SELECT
    gen_random_uuid(),
    t.id,
    'POINT_EXPIRATION',
    true,
    0,
    NULL,
    now(),
    now(),
    NULL
FROM tenants t
WHERE NOT EXISTS (
    SELECT 1
    FROM batch_job_config c
    WHERE c.tenant_id = t.id
      AND c.batch_name = 'POINT_EXPIRATION'
);
