CREATE SCHEMA IF NOT EXISTS timeline;

CREATE TABLE IF NOT EXISTS timeline.timeline_tasks (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    task_status VARCHAR(32) NOT NULL,
    has_time_capital BOOLEAN NOT NULL DEFAULT FALSE,
    estimated_minutes INTEGER,
    deadline DATE,
    capital_cycle_id UUID,
    cycle_start_at TIMESTAMP WITH TIME ZONE,
    cycle_end_at TIMESTAMP WITH TIME ZONE,
    scheduled_start_at TIMESTAMP WITH TIME ZONE,
    scheduled_end_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_timeline_tasks_status
        CHECK (task_status IN ('DRAFT', 'PLANNED', 'SCHEDULED', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED', 'ARCHIVED')),
    CONSTRAINT chk_timeline_tasks_estimated_minutes
        CHECK (estimated_minutes IS NULL OR estimated_minutes > 0),
    CONSTRAINT chk_timeline_tasks_cycle_window
        CHECK (
            (cycle_start_at IS NULL AND cycle_end_at IS NULL)
            OR (cycle_start_at IS NOT NULL AND cycle_end_at IS NOT NULL AND cycle_start_at < cycle_end_at)
        ),
    CONSTRAINT chk_timeline_tasks_scheduled_window
        CHECK (
            (scheduled_start_at IS NULL AND scheduled_end_at IS NULL)
            OR (scheduled_start_at IS NOT NULL AND scheduled_end_at IS NOT NULL AND scheduled_start_at < scheduled_end_at)
        )
);

CREATE TABLE IF NOT EXISTS timeline.timeline_placements (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    task_id UUID NOT NULL,
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(64),
    source VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    conflict_policy VARCHAR(32) NOT NULL,
    conflict_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    is_conflicted BOOLEAN NOT NULL DEFAULT FALSE,
    conflict_reason VARCHAR(500),
    reason VARCHAR(500),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_timeline_placements_task
        FOREIGN KEY (task_id)
        REFERENCES timeline.timeline_tasks(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_timeline_placements_window
        CHECK (start_at < end_at),
    CONSTRAINT chk_timeline_placements_source
        CHECK (source IN ('MANUAL', 'DRAG_DROP', 'SYSTEM')),
    CONSTRAINT chk_timeline_placements_status
        CHECK (status IN ('ACTIVE', 'CANCELLED', 'ARCHIVED')),
    CONSTRAINT chk_timeline_placements_policy
        CHECK (conflict_policy IN ('REJECT', 'WARN', 'ALLOW_WITH_CONFIRMATION'))
);

CREATE TABLE IF NOT EXISTS timeline.timeline_histories (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    action_type VARCHAR(64) NOT NULL,
    placement_id UUID,
    task_id UUID,
    old_value TEXT,
    new_value TEXT,
    reason VARCHAR(1000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_timeline_histories_placement
        FOREIGN KEY (placement_id)
        REFERENCES timeline.timeline_placements(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_timeline_histories_task
        FOREIGN KEY (task_id)
        REFERENCES timeline.timeline_tasks(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_timeline_histories_action
        CHECK (action_type IN (
            'TASK_SNAPSHOT_REGISTERED',
            'TASK_SNAPSHOT_UPDATED',
            'TIMELINE_SCHEDULED',
            'TIMELINE_RESCHEDULED',
            'TIMELINE_MOVED',
            'TIMELINE_CANCELLED',
            'TIMELINE_ARCHIVED',
            'TIMELINE_CONFLICT_CONFIRMED'
        ))
);

DO $$
BEGIN
    IF to_regclass('identity.users') IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_timeline_tasks_owner'
              AND conrelid = 'timeline.timeline_tasks'::regclass
        ) THEN
            ALTER TABLE timeline.timeline_tasks
                ADD CONSTRAINT fk_timeline_tasks_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_timeline_placements_owner'
              AND conrelid = 'timeline.timeline_placements'::regclass
        ) THEN
            ALTER TABLE timeline.timeline_placements
                ADD CONSTRAINT fk_timeline_placements_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_timeline_histories_owner'
              AND conrelid = 'timeline.timeline_histories'::regclass
        ) THEN
            ALTER TABLE timeline.timeline_histories
                ADD CONSTRAINT fk_timeline_histories_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_timeline_tasks_owner_status
    ON timeline.timeline_tasks(owner_id, task_status);
CREATE INDEX IF NOT EXISTS idx_timeline_tasks_owner_eligible
    ON timeline.timeline_tasks(owner_id, has_time_capital, estimated_minutes, task_status);
CREATE INDEX IF NOT EXISTS idx_timeline_tasks_cycle
    ON timeline.timeline_tasks(owner_id, capital_cycle_id);

CREATE INDEX IF NOT EXISTS idx_timeline_placements_owner_time
    ON timeline.timeline_placements(owner_id, start_at, end_at)
    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_timeline_placements_owner_status_time
    ON timeline.timeline_placements(owner_id, status, start_at, end_at)
    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_timeline_placements_task_status
    ON timeline.timeline_placements(task_id, status)
    WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_timeline_placements_conflict
    ON timeline.timeline_placements(owner_id, is_conflicted, conflict_confirmed)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_timeline_histories_owner_time
    ON timeline.timeline_histories(owner_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_timeline_histories_task_time
    ON timeline.timeline_histories(task_id, occurred_at)
    WHERE task_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_timeline_histories_placement_time
    ON timeline.timeline_histories(placement_id, occurred_at)
    WHERE placement_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_timeline_histories_action_time
    ON timeline.timeline_histories(action_type, occurred_at);
