-- ============================================================
-- Optional recurring rule and reminder storage.
-- This migration only prepares schema. It does not seed approvals,
-- enable recurring rules, or schedule reminders by default.
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_tasks_id_owner'
          AND conrelid = 'task.tasks'::regclass
    ) THEN
        ALTER TABLE task.tasks
            ADD CONSTRAINT uq_tasks_id_owner UNIQUE (id, owner_id);
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS task.task_feature_policy_approvals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    task_id UUID,
    feature_code VARCHAR(32) NOT NULL,
    approval_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    requested_by UUID,
    decided_at TIMESTAMP WITH TIME ZONE,
    decided_by UUID,
    decision_reason VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_task_feature_policy_id_feature_status
        UNIQUE (id, feature_code, approval_status),
    CONSTRAINT fk_task_feature_policy_task_owner
        FOREIGN KEY (task_id, owner_id)
        REFERENCES task.tasks (id, owner_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_task_feature_policy_feature_code
        CHECK (feature_code IN ('RECURRING_RULE', 'REMINDER')),
    CONSTRAINT chk_task_feature_policy_status
        CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED', 'REVOKED')),
    CONSTRAINT chk_task_feature_policy_decision_state
        CHECK (
            (approval_status = 'PENDING' AND decided_at IS NULL)
            OR (approval_status <> 'PENDING' AND decided_at IS NOT NULL)
        ),
    CONSTRAINT chk_task_feature_policy_reason_not_blank
        CHECK (decision_reason IS NULL OR length(trim(decision_reason)) > 0)
);

CREATE TABLE IF NOT EXISTS task.task_recurring_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    task_id UUID NOT NULL,
    policy_approval_id UUID NOT NULL,
    policy_feature_code VARCHAR(32) NOT NULL DEFAULT 'RECURRING_RULE',
    policy_approval_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    rule_status VARCHAR(32) NOT NULL DEFAULT 'INACTIVE',
    frequency VARCHAR(32) NOT NULL,
    interval_count INTEGER NOT NULL DEFAULT 1,
    days_of_week VARCHAR(64),
    day_of_month INTEGER,
    month_of_year INTEGER,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    next_run_at TIMESTAMP WITH TIME ZONE,
    last_run_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_task_recurring_rules_id_task_owner
        UNIQUE (id, task_id, owner_id),
    CONSTRAINT fk_task_recurring_rules_task_owner
        FOREIGN KEY (task_id, owner_id)
        REFERENCES task.tasks (id, owner_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_recurring_rules_policy_approved
        FOREIGN KEY (policy_approval_id, policy_feature_code, policy_approval_status)
        REFERENCES task.task_feature_policy_approvals (id, feature_code, approval_status)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT chk_task_recurring_rules_policy_feature
        CHECK (policy_feature_code = 'RECURRING_RULE'),
    CONSTRAINT chk_task_recurring_rules_policy_status
        CHECK (policy_approval_status = 'APPROVED'),
    CONSTRAINT chk_task_recurring_rules_status
        CHECK (rule_status IN ('INACTIVE', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_task_recurring_rules_frequency
        CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY', 'CUSTOM')),
    CONSTRAINT chk_task_recurring_rules_interval
        CHECK (interval_count > 0),
    CONSTRAINT chk_task_recurring_rules_day_of_month
        CHECK (day_of_month IS NULL OR day_of_month BETWEEN 1 AND 31),
    CONSTRAINT chk_task_recurring_rules_month_of_year
        CHECK (month_of_year IS NULL OR month_of_year BETWEEN 1 AND 12),
    CONSTRAINT chk_task_recurring_rules_period
        CHECK (ends_at IS NULL OR ends_at >= starts_at),
    CONSTRAINT chk_task_recurring_rules_active_schedule
        CHECK (rule_status <> 'ACTIVE' OR next_run_at IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS task.task_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL,
    task_id UUID NOT NULL,
    recurring_rule_id UUID,
    policy_approval_id UUID NOT NULL,
    policy_feature_code VARCHAR(32) NOT NULL DEFAULT 'REMINDER',
    policy_approval_status VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    reminder_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reminder_kind VARCHAR(32) NOT NULL DEFAULT 'TASK_DUE',
    remind_at TIMESTAMP WITH TIME ZONE NOT NULL,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    delivery_channel VARCHAR(32) NOT NULL DEFAULT 'IN_APP',
    sent_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    failure_reason VARCHAR(1000),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_task_reminders_task_owner
        FOREIGN KEY (task_id, owner_id)
        REFERENCES task.tasks (id, owner_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_reminders_recurring_rule
        FOREIGN KEY (recurring_rule_id, task_id, owner_id)
        REFERENCES task.task_recurring_rules (id, task_id, owner_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_reminders_policy_approved
        FOREIGN KEY (policy_approval_id, policy_feature_code, policy_approval_status)
        REFERENCES task.task_feature_policy_approvals (id, feature_code, approval_status)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT chk_task_reminders_policy_feature
        CHECK (policy_feature_code = 'REMINDER'),
    CONSTRAINT chk_task_reminders_policy_status
        CHECK (policy_approval_status = 'APPROVED'),
    CONSTRAINT chk_task_reminders_status
        CHECK (reminder_status IN ('PENDING', 'SENT', 'SKIPPED', 'CANCELLED', 'FAILED')),
    CONSTRAINT chk_task_reminders_kind
        CHECK (reminder_kind IN ('TASK_DUE', 'TASK_START', 'CUSTOM', 'RECURRING_INSTANCE')),
    CONSTRAINT chk_task_reminders_delivery_channel
        CHECK (delivery_channel IN ('IN_APP', 'EMAIL', 'PUSH', 'SMS')),
    CONSTRAINT chk_task_reminders_sent_state
        CHECK (reminder_status <> 'SENT' OR sent_at IS NOT NULL),
    CONSTRAINT chk_task_reminders_failed_state
        CHECK (reminder_status <> 'FAILED' OR failed_at IS NOT NULL),
    CONSTRAINT chk_task_reminders_failure_reason_not_blank
        CHECK (failure_reason IS NULL OR length(trim(failure_reason)) > 0)
);

DO $$
BEGIN
    IF to_regclass('identity.users') IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_feature_policy_owner'
              AND conrelid = 'task.task_feature_policy_approvals'::regclass
        ) THEN
            ALTER TABLE task.task_feature_policy_approvals
                ADD CONSTRAINT fk_task_feature_policy_owner
                FOREIGN KEY (owner_id)
                REFERENCES identity.users (id)
                ON DELETE RESTRICT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_feature_policy_requested_by'
              AND conrelid = 'task.task_feature_policy_approvals'::regclass
        ) THEN
            ALTER TABLE task.task_feature_policy_approvals
                ADD CONSTRAINT fk_task_feature_policy_requested_by
                FOREIGN KEY (requested_by)
                REFERENCES identity.users (id)
                ON DELETE RESTRICT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_feature_policy_decided_by'
              AND conrelid = 'task.task_feature_policy_approvals'::regclass
        ) THEN
            ALTER TABLE task.task_feature_policy_approvals
                ADD CONSTRAINT fk_task_feature_policy_decided_by
                FOREIGN KEY (decided_by)
                REFERENCES identity.users (id)
                ON DELETE RESTRICT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_recurring_rules_created_by'
              AND conrelid = 'task.task_recurring_rules'::regclass
        ) THEN
            ALTER TABLE task.task_recurring_rules
                ADD CONSTRAINT fk_task_recurring_rules_created_by
                FOREIGN KEY (created_by)
                REFERENCES identity.users (id)
                ON DELETE RESTRICT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_recurring_rules_updated_by'
              AND conrelid = 'task.task_recurring_rules'::regclass
        ) THEN
            ALTER TABLE task.task_recurring_rules
                ADD CONSTRAINT fk_task_recurring_rules_updated_by
                FOREIGN KEY (updated_by)
                REFERENCES identity.users (id)
                ON DELETE RESTRICT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_reminders_created_by'
              AND conrelid = 'task.task_reminders'::regclass
        ) THEN
            ALTER TABLE task.task_reminders
                ADD CONSTRAINT fk_task_reminders_created_by
                FOREIGN KEY (created_by)
                REFERENCES identity.users (id)
                ON DELETE RESTRICT;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_task_reminders_updated_by'
              AND conrelid = 'task.task_reminders'::regclass
        ) THEN
            ALTER TABLE task.task_reminders
                ADD CONSTRAINT fk_task_reminders_updated_by
                FOREIGN KEY (updated_by)
                REFERENCES identity.users (id)
                ON DELETE RESTRICT;
        END IF;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_task_feature_policy_owner_feature_status
    ON task.task_feature_policy_approvals (owner_id, feature_code, approval_status, requested_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_feature_policy_task_feature_status
    ON task.task_feature_policy_approvals (task_id, feature_code, approval_status, requested_at DESC, id DESC)
    WHERE task_id IS NOT NULL
      AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_recurring_rules_owner_status_next_run
    ON task.task_recurring_rules (owner_id, rule_status, next_run_at, id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_recurring_rules_task_status
    ON task.task_recurring_rules (task_id, rule_status)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_recurring_rules_policy
    ON task.task_recurring_rules (policy_approval_id);

CREATE INDEX IF NOT EXISTS idx_task_reminders_owner_status_remind_at
    ON task.task_reminders (owner_id, reminder_status, remind_at, id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_reminders_task_remind_at
    ON task.task_reminders (task_id, remind_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_task_reminders_recurring_rule
    ON task.task_reminders (recurring_rule_id)
    WHERE recurring_rule_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_task_reminders_policy
    ON task.task_reminders (policy_approval_id);

COMMENT ON TABLE task.task_feature_policy_approvals IS
    'Stores explicit approval decisions for optional task recurring and reminder features. This migration seeds no approvals and enables no optional feature.';

COMMENT ON TABLE task.task_recurring_rules IS
    'Stores recurring task rules. A row can only reference an APPROVED RECURRING_RULE policy decision.';

COMMENT ON TABLE task.task_reminders IS
    'Stores task reminder schedules. A row can only reference an APPROVED REMINDER policy decision.';
