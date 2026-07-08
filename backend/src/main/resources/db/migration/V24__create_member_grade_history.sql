-- 회원 등급(membership_grade) 변경 이력 테이블.
-- 상태 변경 이력(member_status_history)과 별도로, 등급 전이만 독립적으로 기록한다.
-- 등급은 membership_grades FK(UUID)로 관리되므로 이력에도 from/to 등급 id를 UUID로 기록한다.
CREATE TABLE IF NOT EXISTS member_grade_history (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    member_id UUID NOT NULL,
    changed_by UUID,
    from_grade_id UUID,
    to_grade_id UUID NOT NULL,
    reason VARCHAR(500),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_member_grade_history_tenant_member
    ON member_grade_history (tenant_id, member_id, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_member_grade_history_tenant_changed_at
    ON member_grade_history (tenant_id, changed_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_grade_history_tenant') THEN
        ALTER TABLE member_grade_history
            ADD CONSTRAINT fk_member_grade_history_tenant
                FOREIGN KEY (tenant_id) REFERENCES tenants (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_grade_history_member') THEN
        ALTER TABLE member_grade_history
            ADD CONSTRAINT fk_member_grade_history_member
                FOREIGN KEY (member_id) REFERENCES members (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_grade_history_from_grade') THEN
        ALTER TABLE member_grade_history
            ADD CONSTRAINT fk_member_grade_history_from_grade
                FOREIGN KEY (from_grade_id) REFERENCES membership_grades (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_grade_history_to_grade') THEN
        ALTER TABLE member_grade_history
            ADD CONSTRAINT fk_member_grade_history_to_grade
                FOREIGN KEY (to_grade_id) REFERENCES membership_grades (id);
    END IF;
END $$;
