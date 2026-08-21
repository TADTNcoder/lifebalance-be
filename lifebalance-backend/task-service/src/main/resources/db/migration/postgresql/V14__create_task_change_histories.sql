CREATE TABLE IF NOT EXISTS task.task_change_histories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    actor_id UUID,
    task_id UUID NOT NULL,
    timeline_placement_id UUID,
    action_type VARCHAR(64) NOT NULL,
    field_name VARCHAR(64),
    old_value TEXT,
    new_value TEXT,
    reason TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_task_change_histories_task
        FOREIGN KEY (task_id)
        REFERENCES task.tasks(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_task_change_histories_timeline_placement
        FOREIGN KEY (timeline_placement_id)
        REFERENCES task.timeline_placements(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_task_change_histories_action
        CHECK (action_type IN (
            'TASK_CREATED',
            'TASK_UPDATED',
            'TASK_STATUS_CHANGED',
            'TASK_ARCHIVED',
            'TASK_RESTORED',
            'TASK_DELETED',
            'TIMELINE_SCHEDULED',
            'TIMELINE_RESCHEDULED',
            'TIMELINE_MOVED',
            'TIMELINE_CANCELLED'
        ))
);

DO $$
BEGIN
    IF to_regclass('identity.users') IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_change_histories_owner'
              AND conrelid = 'task.task_change_histories'::regclass
        ) THEN
            ALTER TABLE task.task_change_histories
                ADD CONSTRAINT fk_task_change_histories_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_change_histories_actor'
              AND conrelid = 'task.task_change_histories'::regclass
        ) THEN
            ALTER TABLE task.task_change_histories
                ADD CONSTRAINT fk_task_change_histories_actor
                FOREIGN KEY (actor_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_task_change_histories_owner_time
    ON task.task_change_histories (owner_id, occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_task_change_histories_task_time
    ON task.task_change_histories (task_id, occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_task_change_histories_action_time
    ON task.task_change_histories (action_type, occurred_at DESC);

CREATE INDEX IF NOT EXISTS idx_task_change_histories_timeline_time
    ON task.task_change_histories (timeline_placement_id, occurred_at DESC)
    WHERE timeline_placement_id IS NOT NULL;
