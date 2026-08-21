CREATE TABLE IF NOT EXISTS task.task_recurring_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    task_id UUID NOT NULL,
    policy_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_APPROVAL',
    feature_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    recurrence_type VARCHAR(32) NOT NULL,
    interval_count INTEGER NOT NULL DEFAULT 1,
    days_of_week VARCHAR(64),
    starts_on DATE NOT NULL,
    ends_on DATE,
    max_occurrences INTEGER,
    timezone VARCHAR(64),
    reason TEXT,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_task_recurring_rules_task
        FOREIGN KEY (task_id)
        REFERENCES task.tasks(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_task_recurring_rules_policy_status
        CHECK (policy_status IN ('PENDING_APPROVAL', 'APPROVED', 'DISABLED')),

    CONSTRAINT chk_task_recurring_rules_feature_policy
        CHECK (feature_enabled = FALSE OR policy_status = 'APPROVED'),

    CONSTRAINT chk_task_recurring_rules_type
        CHECK (recurrence_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),

    CONSTRAINT chk_task_recurring_rules_interval
        CHECK (interval_count > 0),

    CONSTRAINT chk_task_recurring_rules_occurrences
        CHECK (max_occurrences IS NULL OR max_occurrences > 0),

    CONSTRAINT chk_task_recurring_rules_date_window
        CHECK (ends_on IS NULL OR starts_on <= ends_on)
);

CREATE TABLE IF NOT EXISTS task.task_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    task_id UUID NOT NULL,
    policy_status VARCHAR(32) NOT NULL DEFAULT 'PENDING_APPROVAL',
    feature_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    remind_at TIMESTAMPTZ NOT NULL,
    channel VARCHAR(32) NOT NULL DEFAULT 'IN_APP',
    message TEXT,
    sent_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    reason TEXT,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_task_reminders_task
        FOREIGN KEY (task_id)
        REFERENCES task.tasks(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_task_reminders_policy_status
        CHECK (policy_status IN ('PENDING_APPROVAL', 'APPROVED', 'DISABLED')),

    CONSTRAINT chk_task_reminders_feature_policy
        CHECK (feature_enabled = FALSE OR policy_status = 'APPROVED'),

    CONSTRAINT chk_task_reminders_channel
        CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH'))
);

DO $$
BEGIN
    IF to_regclass('identity.users') IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_recurring_rules_owner'
              AND conrelid = 'task.task_recurring_rules'::regclass
        ) THEN
            ALTER TABLE task.task_recurring_rules
                ADD CONSTRAINT fk_task_recurring_rules_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_reminders_owner'
              AND conrelid = 'task.task_reminders'::regclass
        ) THEN
            ALTER TABLE task.task_reminders
                ADD CONSTRAINT fk_task_reminders_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users(id)
                ON DELETE RESTRICT
                NOT VALID;
        END IF;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_task_recurring_rules_owner_task
    ON task.task_recurring_rules (owner_id, task_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_recurring_rules_policy
    ON task.task_recurring_rules (policy_status, feature_enabled);

CREATE INDEX IF NOT EXISTS idx_task_reminders_owner_time
    ON task.task_reminders (owner_id, remind_at)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_reminders_policy_time
    ON task.task_reminders (policy_status, feature_enabled, remind_at);
