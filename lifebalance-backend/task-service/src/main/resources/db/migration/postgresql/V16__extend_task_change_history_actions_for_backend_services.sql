ALTER TABLE task.task_change_histories
    DROP CONSTRAINT IF EXISTS chk_task_change_histories_action;

ALTER TABLE task.task_change_histories
    ADD CONSTRAINT chk_task_change_histories_action
        CHECK (action_type IN (
            'TASK_CREATED',
            'TASK_UPDATED',
            'TASK_STATUS_CHANGED',
            'TASK_ARCHIVED',
            'TASK_RESTORED',
            'TASK_DELETED',
            'TASK_PLANNED',
            'TASK_PROGRESS_UPDATED',
            'TASK_TAG_ASSIGNED',
            'TASK_TAG_REMOVED',
            'TASK_RECURRING_RULE_CREATED',
            'TASK_RECURRING_RULE_UPDATED',
            'TASK_RECURRING_RULE_DISABLED',
            'TASK_REMINDER_CREATED',
            'TASK_REMINDER_UPDATED',
            'TASK_REMINDER_CANCELLED',
            'TIMELINE_SCHEDULED',
            'TIMELINE_RESCHEDULED',
            'TIMELINE_MOVED',
            'TIMELINE_CANCELLED'
        ));

CREATE INDEX IF NOT EXISTS idx_task_change_histories_owner_action_time
    ON task.task_change_histories (owner_id, action_type, occurred_at DESC);
