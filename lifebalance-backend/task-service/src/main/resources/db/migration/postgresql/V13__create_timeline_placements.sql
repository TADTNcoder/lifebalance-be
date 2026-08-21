CREATE TABLE IF NOT EXISTS task.timeline_placements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    user_id UUID NOT NULL,
    task_id UUID NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    timezone VARCHAR(64),
    source VARCHAR(32) NOT NULL DEFAULT 'MANUAL',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    reason VARCHAR(500),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_timeline_placements_task
        FOREIGN KEY (task_id)
        REFERENCES task.tasks(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_timeline_placements_window
        CHECK (start_at < end_at),

    CONSTRAINT chk_timeline_placements_status
        CHECK (status IN ('ACTIVE', 'CANCELLED', 'ARCHIVED')),

    CONSTRAINT chk_timeline_placements_source
        CHECK (source IN ('MANUAL', 'DRAG_DROP', 'SYSTEM'))
);

DO $$
BEGIN
    IF to_regclass('identity.users') IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_timeline_placements_owner'
              AND conrelid = 'task.timeline_placements'::regclass
        ) THEN
            ALTER TABLE task.timeline_placements
                ADD CONSTRAINT fk_timeline_placements_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_timeline_placements_created_by'
              AND conrelid = 'task.timeline_placements'::regclass
        ) THEN
            ALTER TABLE task.timeline_placements
                ADD CONSTRAINT fk_timeline_placements_created_by
                FOREIGN KEY (created_by)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_timeline_placements_updated_by'
              AND conrelid = 'task.timeline_placements'::regclass
        ) THEN
            ALTER TABLE task.timeline_placements
                ADD CONSTRAINT fk_timeline_placements_updated_by
                FOREIGN KEY (updated_by)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_timeline_placements_owner_time
    ON task.timeline_placements (owner_id, start_at, end_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_timeline_placements_task_status
    ON task.timeline_placements (task_id, status);

CREATE INDEX IF NOT EXISTS idx_timeline_placements_owner_status_time
    ON task.timeline_placements (owner_id, status, start_at, end_at)
    WHERE deleted_at IS NULL;
