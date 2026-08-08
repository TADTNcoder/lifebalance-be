ALTER TABLE task.tasks
    ADD COLUMN IF NOT EXISTS user_id UUID;

UPDATE task.tasks
SET user_id = '00000000-0000-0000-0000-000000000000'
WHERE user_id IS NULL;

ALTER TABLE task.tasks
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE task.tasks
    RENAME COLUMN task_name TO name;

ALTER TABLE task.tasks
    RENAME COLUMN priority_level TO priority;

ALTER TABLE task.tasks
    RENAME COLUMN end_date TO deadline;

ALTER TABLE task.tasks
    ADD COLUMN IF NOT EXISTS progress INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS estimated_minutes INTEGER,
    ADD COLUMN IF NOT EXISTS estimated_cost DECIMAL(19, 4),
    ADD COLUMN IF NOT EXISTS category_id UUID;

UPDATE task.tasks
SET status = CASE status
    WHEN 'TODO' THEN 'DRAFT'
    ELSE status
END;

ALTER TABLE task.tasks
    DROP COLUMN IF EXISTS start_date,
    DROP COLUMN IF EXISTS start_time,
    DROP COLUMN IF EXISTS end_time,
    DROP COLUMN IF EXISTS day_of_week,
    DROP COLUMN IF EXISTS note;

DROP INDEX IF EXISTS task.idx_task_status;
DROP INDEX IF EXISTS task.idx_task_priority;

ALTER TABLE task.tasks
    ALTER COLUMN name TYPE VARCHAR(255),
    ALTER COLUMN description TYPE VARCHAR(2000),
    ALTER COLUMN status TYPE VARCHAR(32),
    ALTER COLUMN priority TYPE VARCHAR(32);

ALTER TABLE task.tasks
    ADD CONSTRAINT fk_tasks_category
        FOREIGN KEY (category_id)
        REFERENCES task.categories (id)
        ON DELETE SET NULL,
    ADD CONSTRAINT chk_tasks_status
        CHECK (status IN (
            'DRAFT',
            'PLANNED',
            'SCHEDULED',
            'IN_PROGRESS',
            'ON_HOLD',
            'COMPLETED',
            'CANCELLED',
            'ARCHIVED'
        )),
    ADD CONSTRAINT chk_tasks_priority
        CHECK (priority IS NULL OR priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    ADD CONSTRAINT chk_tasks_progress
        CHECK (progress BETWEEN 0 AND 100),
    ADD CONSTRAINT chk_tasks_estimated_minutes
        CHECK (estimated_minutes IS NULL OR estimated_minutes >= 0),
    ADD CONSTRAINT chk_tasks_estimated_cost
        CHECK (estimated_cost IS NULL OR estimated_cost >= 0),
    ADD CONSTRAINT chk_tasks_scheduled_estimated_minutes
        CHECK (status <> 'SCHEDULED' OR estimated_minutes > 0);

CREATE INDEX idx_tasks_user_status
    ON task.tasks (user_id, status);

CREATE INDEX idx_tasks_user_deadline
    ON task.tasks (user_id, deadline);

CREATE INDEX idx_tasks_priority
    ON task.tasks (priority);

CREATE INDEX idx_tasks_category
    ON task.tasks (category_id);
