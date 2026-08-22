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

ALTER TABLE task.task_recurring_rules
    ADD COLUMN IF NOT EXISTS policy_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS feature_enabled BOOLEAN,
    ADD COLUMN IF NOT EXISTS recurrence_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS starts_on DATE,
    ADD COLUMN IF NOT EXISTS ends_on DATE,
    ADD COLUMN IF NOT EXISTS max_occurrences INTEGER,
    ADD COLUMN IF NOT EXISTS reason TEXT;

ALTER TABLE task.task_reminders
    ADD COLUMN IF NOT EXISTS policy_status VARCHAR(32),
    ADD COLUMN IF NOT EXISTS feature_enabled BOOLEAN,
    ADD COLUMN IF NOT EXISTS channel VARCHAR(32),
    ADD COLUMN IF NOT EXISTS message TEXT,
    ADD COLUMN IF NOT EXISTS reason TEXT;

UPDATE task.task_recurring_rules
SET policy_status = CASE
        WHEN policy_approval_status = 'APPROVED' THEN 'APPROVED'
        WHEN policy_approval_status = 'REVOKED' THEN 'DISABLED'
        ELSE 'PENDING_APPROVAL'
    END
WHERE policy_status IS NULL;

UPDATE task.task_recurring_rules
SET feature_enabled = FALSE
WHERE feature_enabled IS NULL;

UPDATE task.task_recurring_rules
SET recurrence_type = CASE
        WHEN frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') THEN frequency
        ELSE 'DAILY'
    END
WHERE recurrence_type IS NULL;

UPDATE task.task_recurring_rules
SET starts_on = COALESCE(starts_at::date, CURRENT_DATE)
WHERE starts_on IS NULL;

UPDATE task.task_recurring_rules
SET ends_on = ends_at::date
WHERE ends_on IS NULL
  AND ends_at IS NOT NULL;

UPDATE task.task_reminders
SET policy_status = CASE
        WHEN policy_approval_status = 'APPROVED' THEN 'APPROVED'
        WHEN policy_approval_status = 'REVOKED' THEN 'DISABLED'
        ELSE 'PENDING_APPROVAL'
    END
WHERE policy_status IS NULL;

UPDATE task.task_reminders
SET feature_enabled = FALSE
WHERE feature_enabled IS NULL;

UPDATE task.task_reminders
SET channel = CASE
        WHEN delivery_channel IN ('IN_APP', 'EMAIL', 'PUSH') THEN delivery_channel
        ELSE 'IN_APP'
    END
WHERE channel IS NULL;

CREATE OR REPLACE FUNCTION task.normalize_task_recurring_rule_columns()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.policy_status IS NULL THEN
        NEW.policy_status := CASE
            WHEN NEW.policy_approval_status = 'APPROVED' THEN 'APPROVED'
            WHEN NEW.policy_approval_status = 'REVOKED' THEN 'DISABLED'
            ELSE 'PENDING_APPROVAL'
        END;
    END IF;

    IF NEW.feature_enabled IS NULL THEN
        NEW.feature_enabled := FALSE;
    END IF;

    IF NEW.recurrence_type IS NULL THEN
        NEW.recurrence_type := CASE
            WHEN NEW.frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY') THEN NEW.frequency
            ELSE 'DAILY'
        END;
    END IF;

    IF NEW.starts_on IS NULL THEN
        NEW.starts_on := COALESCE(NEW.starts_at::date, CURRENT_DATE);
    END IF;

    IF NEW.ends_on IS NULL AND NEW.ends_at IS NOT NULL THEN
        NEW.ends_on := NEW.ends_at::date;
    END IF;

    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION task.normalize_task_reminder_columns()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.policy_status IS NULL THEN
        NEW.policy_status := CASE
            WHEN NEW.policy_approval_status = 'APPROVED' THEN 'APPROVED'
            WHEN NEW.policy_approval_status = 'REVOKED' THEN 'DISABLED'
            ELSE 'PENDING_APPROVAL'
        END;
    END IF;

    IF NEW.feature_enabled IS NULL THEN
        NEW.feature_enabled := FALSE;
    END IF;

    IF NEW.channel IS NULL THEN
        NEW.channel := CASE
            WHEN NEW.delivery_channel IN ('IN_APP', 'EMAIL', 'PUSH') THEN NEW.delivery_channel
            ELSE 'IN_APP'
        END;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_normalize_task_recurring_rule_columns ON task.task_recurring_rules;
CREATE TRIGGER trg_normalize_task_recurring_rule_columns
    BEFORE INSERT OR UPDATE ON task.task_recurring_rules
    FOR EACH ROW
    EXECUTE FUNCTION task.normalize_task_recurring_rule_columns();

DROP TRIGGER IF EXISTS trg_normalize_task_reminder_columns ON task.task_reminders;
CREATE TRIGGER trg_normalize_task_reminder_columns
    BEFORE INSERT OR UPDATE ON task.task_reminders
    FOR EACH ROW
    EXECUTE FUNCTION task.normalize_task_reminder_columns();

ALTER TABLE task.task_recurring_rules
    ALTER COLUMN policy_status SET DEFAULT 'PENDING_APPROVAL',
    ALTER COLUMN policy_status SET NOT NULL,
    ALTER COLUMN feature_enabled SET DEFAULT FALSE,
    ALTER COLUMN feature_enabled SET NOT NULL,
    ALTER COLUMN recurrence_type SET NOT NULL,
    ALTER COLUMN starts_on SET NOT NULL,
    ALTER COLUMN policy_approval_id DROP NOT NULL,
    ALTER COLUMN frequency DROP NOT NULL,
    ALTER COLUMN starts_at DROP NOT NULL;

ALTER TABLE task.task_reminders
    ALTER COLUMN policy_status SET DEFAULT 'PENDING_APPROVAL',
    ALTER COLUMN policy_status SET NOT NULL,
    ALTER COLUMN feature_enabled SET DEFAULT FALSE,
    ALTER COLUMN feature_enabled SET NOT NULL,
    ALTER COLUMN channel SET DEFAULT 'IN_APP',
    ALTER COLUMN channel SET NOT NULL,
    ALTER COLUMN policy_approval_id DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_recurring_rules_optional_policy_status'
          AND conrelid = 'task.task_recurring_rules'::regclass
    ) THEN
        ALTER TABLE task.task_recurring_rules
            ADD CONSTRAINT chk_task_recurring_rules_optional_policy_status
            CHECK (policy_status IN ('PENDING_APPROVAL', 'APPROVED', 'DISABLED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_recurring_rules_feature_policy'
          AND conrelid = 'task.task_recurring_rules'::regclass
    ) THEN
        ALTER TABLE task.task_recurring_rules
            ADD CONSTRAINT chk_task_recurring_rules_feature_policy
            CHECK (feature_enabled = FALSE OR policy_status = 'APPROVED');
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_recurring_rules_type'
          AND conrelid = 'task.task_recurring_rules'::regclass
    ) THEN
        ALTER TABLE task.task_recurring_rules
            ADD CONSTRAINT chk_task_recurring_rules_type
            CHECK (recurrence_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'));
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_recurring_rules_occurrences'
          AND conrelid = 'task.task_recurring_rules'::regclass
    ) THEN
        ALTER TABLE task.task_recurring_rules
            ADD CONSTRAINT chk_task_recurring_rules_occurrences
            CHECK (max_occurrences IS NULL OR max_occurrences > 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_recurring_rules_date_window'
          AND conrelid = 'task.task_recurring_rules'::regclass
    ) THEN
        ALTER TABLE task.task_recurring_rules
            ADD CONSTRAINT chk_task_recurring_rules_date_window
            CHECK (ends_on IS NULL OR starts_on <= ends_on);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_reminders_optional_policy_status'
          AND conrelid = 'task.task_reminders'::regclass
    ) THEN
        ALTER TABLE task.task_reminders
            ADD CONSTRAINT chk_task_reminders_optional_policy_status
            CHECK (policy_status IN ('PENDING_APPROVAL', 'APPROVED', 'DISABLED'));
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_reminders_feature_policy'
          AND conrelid = 'task.task_reminders'::regclass
    ) THEN
        ALTER TABLE task.task_reminders
            ADD CONSTRAINT chk_task_reminders_feature_policy
            CHECK (feature_enabled = FALSE OR policy_status = 'APPROVED');
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_task_reminders_channel'
          AND conrelid = 'task.task_reminders'::regclass
    ) THEN
        ALTER TABLE task.task_reminders
            ADD CONSTRAINT chk_task_reminders_channel
            CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH'));
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
