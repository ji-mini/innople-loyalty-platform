-- 회원 상태 변경 이력 테이블.
-- 회원 상태(status_code)는 공통코드(MEMBER_STATUS 그룹) 기반 문자열이므로 이력도 문자열로 기록한다.
CREATE TABLE IF NOT EXISTS member_status_history (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL,
    changed_by UUID,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_member_status_history_tenant_member
    ON member_status_history (tenant_id, member_id, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_member_status_history_tenant_changed_at
    ON member_status_history (tenant_id, changed_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_status_history_tenant') THEN
        ALTER TABLE member_status_history
            ADD CONSTRAINT fk_member_status_history_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_status_history_member') THEN
        ALTER TABLE member_status_history
            ADD CONSTRAINT fk_member_status_history_member
                FOREIGN KEY (member_id) REFERENCES members (id);
    END IF;
END $$;
