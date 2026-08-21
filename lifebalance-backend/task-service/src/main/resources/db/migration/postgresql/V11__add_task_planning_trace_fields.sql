ALTER TABLE task.tasks
    ADD COLUMN IF NOT EXISTS planned_start_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS planned_end_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS scheduled_start_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS scheduled_end_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS archived_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID;

UPDATE task.tasks
SET created_by = owner_id
WHERE created_by IS NULL
  AND owner_id IS NOT NULL;

UPDATE task.tasks
SET updated_by = owner_id
WHERE updated_by IS NULL
  AND owner_id IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_tasks_planned_window'
          AND conrelid = 'task.tasks'::regclass
    ) THEN
        ALTER TABLE task.tasks
            ADD CONSTRAINT chk_tasks_planned_window
            CHECK (planned_start_at IS NULL AND planned_end_at IS NULL
                OR planned_start_at IS NOT NULL AND planned_end_at IS NOT NULL AND planned_start_at < planned_end_at);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_tasks_scheduled_window'
          AND conrelid = 'task.tasks'::regclass
    ) THEN
        ALTER TABLE task.tasks
            ADD CONSTRAINT chk_tasks_scheduled_window
            CHECK (scheduled_start_at IS NULL AND scheduled_end_at IS NULL
                OR scheduled_start_at IS NOT NULL AND scheduled_end_at IS NOT NULL AND scheduled_start_at < scheduled_end_at);
    END IF;

    IF to_regclass('identity.users') IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_tasks_created_by'
              AND conrelid = 'task.tasks'::regclass
        ) THEN
            ALTER TABLE task.tasks
                ADD CONSTRAINT fk_tasks_created_by
                FOREIGN KEY (created_by)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_tasks_updated_by'
              AND conrelid = 'task.tasks'::regclass
        ) THEN
            ALTER TABLE task.tasks
                ADD CONSTRAINT fk_tasks_updated_by
                FOREIGN KEY (updated_by)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_tasks_owner_status_deadline
    ON task.tasks (owner_id, status, deadline);

CREATE INDEX IF NOT EXISTS idx_tasks_owner_scheduled_start
    ON task.tasks (owner_id, scheduled_start_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_tasks_owner_updated
    ON task.tasks (owner_id, updated_at DESC);
