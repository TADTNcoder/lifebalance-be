CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE IF NOT EXISTS notification.notifications (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    actor_id UUID,
    event_type VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    notification_status VARCHAR(16) NOT NULL,
    delivery_status VARCHAR(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    reference_type VARCHAR(64),
    reference_id UUID,
    purpose VARCHAR(500) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    read_at TIMESTAMP WITH TIME ZONE,
    archived_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    failure_reason VARCHAR(1000),
    provider_message_id VARCHAR(200),
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_notifications_event_type
        CHECK (event_type IN (
            'TASK_REMINDER',
            'TIMELINE_CHANGE',
            'CAPITAL_ALERT',
            'FINANCE_ALERT',
            'ACTUAL_RECORDING_REMINDER',
            'SYSTEM_ANNOUNCEMENT',
            'SUPPORT_UPDATE',
            'SECURITY_ALERT',
            'GENERAL'
        )),
    CONSTRAINT chk_notifications_channel
        CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH', 'SMS', 'WEBHOOK')),
    CONSTRAINT chk_notifications_priority
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_notifications_status
        CHECK (notification_status IN ('UNREAD', 'READ', 'ARCHIVED')),
    CONSTRAINT chk_notifications_delivery_status
        CHECK (delivery_status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED', 'SKIPPED')),
    CONSTRAINT chk_notifications_retry_count
        CHECK (retry_count >= 0),
    CONSTRAINT chk_notifications_title
        CHECK (length(trim(title)) > 0),
    CONSTRAINT chk_notifications_message
        CHECK (length(trim(message)) > 0),
    CONSTRAINT chk_notifications_purpose
        CHECK (length(trim(purpose)) > 0),
    CONSTRAINT chk_notifications_reference_pair
        CHECK ((reference_type IS NULL AND reference_id IS NULL) OR (reference_type IS NOT NULL AND reference_id IS NOT NULL))
);

CREATE TABLE IF NOT EXISTS notification.notification_preferences (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(64),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_notification_preferences_owner_event_channel
        UNIQUE (owner_id, event_type, channel),
    CONSTRAINT chk_notification_preferences_event_type
        CHECK (event_type IN (
            'TASK_REMINDER',
            'TIMELINE_CHANGE',
            'CAPITAL_ALERT',
            'FINANCE_ALERT',
            'ACTUAL_RECORDING_REMINDER',
            'SYSTEM_ANNOUNCEMENT',
            'SUPPORT_UPDATE',
            'SECURITY_ALERT',
            'GENERAL'
        )),
    CONSTRAINT chk_notification_preferences_channel
        CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH', 'SMS', 'WEBHOOK')),
    CONSTRAINT chk_notification_preferences_quiet_hours
        CHECK (
            (quiet_hours_start IS NULL AND quiet_hours_end IS NULL)
            OR (quiet_hours_start IS NOT NULL AND quiet_hours_end IS NOT NULL)
        )
);

CREATE TABLE IF NOT EXISTS notification.notification_templates (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    template_key VARCHAR(120) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    title_template VARCHAR(200) NOT NULL,
    message_template VARCHAR(2000) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_notification_templates_owner_key_channel
        UNIQUE (owner_id, template_key, channel),
    CONSTRAINT chk_notification_templates_event_type
        CHECK (event_type IN (
            'TASK_REMINDER',
            'TIMELINE_CHANGE',
            'CAPITAL_ALERT',
            'FINANCE_ALERT',
            'ACTUAL_RECORDING_REMINDER',
            'SYSTEM_ANNOUNCEMENT',
            'SUPPORT_UPDATE',
            'SECURITY_ALERT',
            'GENERAL'
        )),
    CONSTRAINT chk_notification_templates_channel
        CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH', 'SMS', 'WEBHOOK')),
    CONSTRAINT chk_notification_templates_key
        CHECK (length(trim(template_key)) > 0),
    CONSTRAINT chk_notification_templates_title
        CHECK (length(trim(title_template)) > 0),
    CONSTRAINT chk_notification_templates_message
        CHECK (length(trim(message_template)) > 0)
);

CREATE TABLE IF NOT EXISTS notification.notification_delivery_attempts (
    id UUID PRIMARY KEY,
    notification_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    channel VARCHAR(32) NOT NULL,
    attempt_number INTEGER NOT NULL,
    delivery_status VARCHAR(16) NOT NULL,
    provider_message_id VARCHAR(200),
    error_message VARCHAR(1000),
    attempted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_delivery_attempts_notification
        FOREIGN KEY (notification_id)
        REFERENCES notification.notifications(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_notification_delivery_attempts_channel
        CHECK (channel IN ('IN_APP', 'EMAIL', 'PUSH', 'SMS', 'WEBHOOK')),
    CONSTRAINT chk_notification_delivery_attempts_status
        CHECK (delivery_status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED', 'SKIPPED')),
    CONSTRAINT chk_notification_delivery_attempts_number
        CHECK (attempt_number > 0)
);

CREATE TABLE IF NOT EXISTS notification.notification_histories (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    action_type VARCHAR(64) NOT NULL,
    notification_id UUID,
    old_value TEXT,
    new_value TEXT,
    reason VARCHAR(1000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_histories_notification
        FOREIGN KEY (notification_id)
        REFERENCES notification.notifications(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_notification_histories_action
        CHECK (action_type IN (
            'NOTIFICATION_CREATED',
            'NOTIFICATION_SKIPPED',
            'NOTIFICATION_QUEUED',
            'NOTIFICATION_SENT',
            'NOTIFICATION_FAILED',
            'NOTIFICATION_RETRIED',
            'NOTIFICATION_READ',
            'NOTIFICATION_UNREAD',
            'NOTIFICATION_ARCHIVED',
            'PREFERENCE_UPDATED',
            'TEMPLATE_CREATED',
            'TEMPLATE_UPDATED',
            'TEMPLATE_ARCHIVED'
        ))
);

CREATE INDEX IF NOT EXISTS idx_notifications_owner_status_time
    ON notification.notifications(owner_id, notification_status, created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_owner_event_time
    ON notification.notifications(owner_id, event_type, created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_owner_channel_delivery
    ON notification.notifications(owner_id, channel, delivery_status, scheduled_at);
CREATE INDEX IF NOT EXISTS idx_notifications_reference
    ON notification.notifications(reference_type, reference_id, created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_pending_delivery
    ON notification.notifications(delivery_status, scheduled_at, channel);

CREATE INDEX IF NOT EXISTS idx_notification_preferences_owner_channel
    ON notification.notification_preferences(owner_id, channel);
CREATE INDEX IF NOT EXISTS idx_notification_preferences_created_by_created_at
    ON notification.notification_preferences(created_by, created_at, id);
CREATE INDEX IF NOT EXISTS idx_notification_preferences_updated_by_updated_at
    ON notification.notification_preferences(updated_by, updated_at, id);
CREATE INDEX IF NOT EXISTS idx_notification_templates_owner_key
    ON notification.notification_templates(owner_id, template_key, enabled);
CREATE INDEX IF NOT EXISTS idx_notification_templates_created_by_created_at
    ON notification.notification_templates(created_by, created_at, id);
CREATE INDEX IF NOT EXISTS idx_notification_templates_updated_by_updated_at
    ON notification.notification_templates(updated_by, updated_at, id);
CREATE INDEX IF NOT EXISTS idx_notification_delivery_attempts_notification_time
    ON notification.notification_delivery_attempts(notification_id, attempted_at);
CREATE INDEX IF NOT EXISTS idx_notification_histories_owner_time
    ON notification.notification_histories(owner_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_notification_histories_notification_time
    ON notification.notification_histories(notification_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_notifications_created_by_created_at
    ON notification.notifications(created_by, created_at, id);
CREATE INDEX IF NOT EXISTS idx_notifications_updated_by_updated_at
    ON notification.notifications(updated_by, updated_at, id);
