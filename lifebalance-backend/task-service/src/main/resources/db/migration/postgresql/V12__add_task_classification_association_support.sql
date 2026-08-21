ALTER TABLE task.task_tags
    ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_task_tags_task_assigned
    ON task.task_tags (task_id, assigned_at DESC);

CREATE INDEX IF NOT EXISTS idx_tags_user_slug_active
    ON task.tags (user_id, slug)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_categories_system_slug_active
    ON task.categories (is_system, slug)
    WHERE deleted_at IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_tags_not_self_null'
          AND conrelid = 'task.task_tags'::regclass
    ) THEN
        ALTER TABLE task.task_tags
            ADD CONSTRAINT chk_task_tags_not_self_null
            CHECK (task_id IS NOT NULL AND tag_id IS NOT NULL);
    END IF;
END $$;
