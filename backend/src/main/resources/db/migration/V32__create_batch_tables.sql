-- 자동탈퇴 등 배치 기능을 위한 신규 테이블 2종.
-- 테이블/제약은 Hibernate 자동생성에 맡기지 않고 Flyway 로 명시한다(member_ledgers 교훈).
-- enum 성격 컬럼(status)의 CHECK 은 반드시 named 로 생성해 익명 CHECK 드리프트를 방지한다(ck_ 접두 컨벤션).

-- =====================================================================
-- 테이블 1: batch_job_config (테넌트 × 배치 당 1행의 운영 설정)
-- =====================================================================
CREATE TABLE IF NOT EXISTS batch_job_config (
    id             UUID PRIMARY KEY,
    tenant_id      UUID NOT NULL,
    batch_name     VARCHAR(100) NOT NULL,
    enabled        BOOLEAN NOT NULL DEFAULT false,
    run_hour       SMALLINT NOT NULL,
    threshold_days INT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by     UUID,
    CONSTRAINT uk_batch_job_config_tenant_batch UNIQUE (tenant_id, batch_name),
    CONSTRAINT ck_batch_job_config_run_hour CHECK (run_hour BETWEEN 0 AND 23),
    CONSTRAINT ck_batch_job_config_threshold_days CHECK (threshold_days > 0)
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_batch_job_config_tenant') THEN
        ALTER TABLE batch_job_config
            ADD CONSTRAINT fk_batch_job_config_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;
END $$;

-- =====================================================================
-- 테이블 2: batch_execution_history (배치 1회 실행이 테넌트를 처리할 때마다 테넌트별 1행)
--   "오늘 이 테넌트가 이미 처리됐는가"(catch-up 판정)에도 조회한다.
-- =====================================================================
CREATE TABLE IF NOT EXISTS batch_execution_history (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    batch_name      VARCHAR(100) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at     TIMESTAMPTZ,
    processed_count INT NOT NULL DEFAULT 0,
    error_count     INT NOT NULL DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_batch_exec_history_status CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL', 'FAILED')),
    CONSTRAINT ck_batch_exec_history_processed_count CHECK (processed_count >= 0),
    CONSTRAINT ck_batch_exec_history_error_count CHECK (error_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_batch_execution_history_tenant_batch_started
    ON batch_execution_history (tenant_id, batch_name, started_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_batch_execution_history_tenant') THEN
        ALTER TABLE batch_execution_history
            ADD CONSTRAINT fk_batch_execution_history_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;
END $$;
