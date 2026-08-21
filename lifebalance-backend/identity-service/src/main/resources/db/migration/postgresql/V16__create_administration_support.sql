ALTER TABLE identity.audit_logs
    DROP CONSTRAINT IF EXISTS chk_identity_audit_logs_entity_name;

ALTER TABLE identity.audit_logs
    DROP CONSTRAINT IF EXISTS chk_identity_audit_logs_action;

ALTER TABLE identity.audit_logs
    ADD CONSTRAINT chk_identity_audit_logs_entity_name
        CHECK (entity_name IN (
            'AUTHENTICATION',
            'USER',
            'ROLE',
            'PERMISSION',
            'USER_ROLE',
            'ROLE_PERMISSION',
            'SUPPORT_TICKET',
            'SYSTEM_CONFIGURATION',
            'ANNOUNCEMENT',
            'MAINTENANCE'
        ));

ALTER TABLE identity.audit_logs
    ADD CONSTRAINT chk_identity_audit_logs_action
        CHECK (action IN (
            'LOGIN',
            'LOGOUT',
            'CREATE_ROLE',
            'UPDATE_ROLE',
            'DELETE_ROLE',
            'ASSIGN_PERMISSION',
            'ASSIGN_ROLE_PERMISSIONS',
            'REVOKE_PERMISSION',
            'CREATE_PERMISSION',
            'UPDATE_PERMISSION',
            'DELETE_PERMISSION',
            'UPDATE_USER',
            'ACTIVATE_USER',
            'DISABLE_USER',
            'LOCK_USER',
            'UNLOCK_USER',
            'DELETE_USER',
            'ASSIGN_ROLE',
            'REVOKE_ROLE',
            'CREATE_USER',
            'REMOVE_ROLE',
            'REMOVE_PERMISSION',
            'CREATE_SUPPORT_TICKET',
            'UPDATE_SUPPORT_TICKET',
            'ASSIGN_SUPPORT_TICKET',
            'RESOLVE_SUPPORT_TICKET',
            'CLOSE_SUPPORT_TICKET',
            'REOPEN_SUPPORT_TICKET',
            'UPDATE_CONFIGURATION',
            'BROADCAST_ANNOUNCEMENT',
            'UPDATE_MAINTENANCE_STATUS'
        ));

CREATE TABLE IF NOT EXISTS identity.support_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number VARCHAR(32) NOT NULL UNIQUE,
    requester_id UUID NOT NULL,
    assignee_id UUID,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NEW',
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(64) NOT NULL DEFAULT 'OTHER',
    resolution TEXT,
    escalation_reason TEXT,
    received_at TIMESTAMPTZ,
    assigned_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    reopened_at TIMESTAMPTZ,
    last_status_changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_identity_support_tickets_requester
        FOREIGN KEY (requester_id) REFERENCES identity.users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_identity_support_tickets_assignee
        FOREIGN KEY (assignee_id) REFERENCES identity.users(id) ON DELETE SET NULL,
    CONSTRAINT chk_identity_support_tickets_status
        CHECK (status IN ('NEW', 'RECEIVED', 'ASSIGNED', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED', 'CLOSED', 'REOPENED')),
    CONSTRAINT chk_identity_support_tickets_priority
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    CONSTRAINT chk_identity_support_tickets_category
        CHECK (category IN ('ACCOUNT_ACCESS', 'PERMISSION_ISSUE', 'USAGE_QUESTION', 'SYSTEM_ISSUE', 'BILLING', 'OTHER'))
);

CREATE TABLE IF NOT EXISTS identity.support_ticket_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    action VARCHAR(32) NOT NULL,
    previous_status VARCHAR(32),
    new_status VARCHAR(32),
    previous_assignee_id UUID,
    new_assignee_id UUID,
    comment_text TEXT,
    reason TEXT,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_identity_ticket_history_ticket
        FOREIGN KEY (ticket_id) REFERENCES identity.support_tickets(id) ON DELETE RESTRICT,
    CONSTRAINT fk_identity_ticket_history_actor
        FOREIGN KEY (actor_id) REFERENCES identity.users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_identity_ticket_history_action
        CHECK (action IN ('CREATED', 'RECEIVED', 'ASSIGNED', 'UNASSIGNED', 'UPDATED', 'PRIORITY_UPDATED', 'CATEGORY_UPDATED', 'COMMENTED', 'ESCALATED', 'RESOLVED', 'CLOSED', 'REOPENED'))
);

CREATE TABLE IF NOT EXISTS identity.activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID,
    actor_keycloak_id VARCHAR(255),
    actor_username VARCHAR(100),
    category VARCHAR(64) NOT NULL,
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(120),
    summary VARCHAR(500) NOT NULL,
    details TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_identity_activity_logs_actor
        FOREIGN KEY (actor_id) REFERENCES identity.users(id) ON DELETE SET NULL,
    CONSTRAINT chk_identity_activity_logs_category
        CHECK (category IN ('USER_MANAGEMENT', 'STAFF_MANAGEMENT', 'SUPPORT_TICKET', 'CONFIGURATION', 'ANNOUNCEMENT', 'MAINTENANCE', 'AUDIT', 'SYSTEM'))
);

CREATE TABLE IF NOT EXISTS identity.system_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(150) NOT NULL UNIQUE,
    display_name VARCHAR(200) NOT NULL,
    description TEXT,
    config_value TEXT NOT NULL,
    value_type VARCHAR(32) NOT NULL,
    sensitive BOOLEAN NOT NULL DEFAULT false,
    editable BOOLEAN NOT NULL DEFAULT true,
    requires_confirmation BOOLEAN NOT NULL DEFAULT false,
    updated_by UUID,
    last_change_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_identity_system_configurations_updated_by
        FOREIGN KEY (updated_by) REFERENCES identity.users(id) ON DELETE SET NULL,
    CONSTRAINT chk_identity_system_configurations_value_type
        CHECK (value_type IN ('STRING', 'BOOLEAN', 'INTEGER', 'DECIMAL', 'JSON'))
);

CREATE TABLE IF NOT EXISTS identity.system_announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    audience VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ends_at TIMESTAMPTZ,
    published_at TIMESTAMPTZ,
    published_by UUID,
    cancelled_at TIMESTAMPTZ,
    cancellation_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT fk_identity_announcements_published_by
        FOREIGN KEY (published_by) REFERENCES identity.users(id) ON DELETE SET NULL,
    CONSTRAINT chk_identity_announcements_audience
        CHECK (audience IN ('ALL_USERS', 'STAFF', 'ADMINS')),
    CONSTRAINT chk_identity_announcements_status
        CHECK (status IN ('DRAFT', 'SCHEDULED', 'ACTIVE', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT chk_identity_announcements_period
        CHECK (ends_at IS NULL OR ends_at >= starts_at)
);

CREATE INDEX IF NOT EXISTS idx_identity_support_tickets_requester
    ON identity.support_tickets (requester_id, created_at);
CREATE INDEX IF NOT EXISTS idx_identity_support_tickets_assignee
    ON identity.support_tickets (assignee_id, status);
CREATE INDEX IF NOT EXISTS idx_identity_support_tickets_status
    ON identity.support_tickets (status, priority, created_at);
CREATE INDEX IF NOT EXISTS idx_identity_support_tickets_category
    ON identity.support_tickets (category, created_at);
CREATE INDEX IF NOT EXISTS idx_identity_ticket_history_ticket
    ON identity.support_ticket_history (ticket_id, created_at);
CREATE INDEX IF NOT EXISTS idx_identity_ticket_history_actor
    ON identity.support_ticket_history (actor_id, created_at);
CREATE INDEX IF NOT EXISTS idx_identity_ticket_history_action
    ON identity.support_ticket_history (action, created_at);
CREATE INDEX IF NOT EXISTS idx_identity_activity_logs_actor
    ON identity.activity_logs (actor_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_identity_activity_logs_category
    ON identity.activity_logs (category, occurred_at);
CREATE INDEX IF NOT EXISTS idx_identity_activity_logs_entity
    ON identity.activity_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_identity_activity_logs_action
    ON identity.activity_logs (action, occurred_at);
CREATE INDEX IF NOT EXISTS idx_identity_system_configurations_key
    ON identity.system_configurations (config_key);
CREATE INDEX IF NOT EXISTS idx_identity_system_configurations_editable
    ON identity.system_configurations (editable, sensitive);
CREATE INDEX IF NOT EXISTS idx_identity_announcements_status
    ON identity.system_announcements (status, starts_at, ends_at);
CREATE INDEX IF NOT EXISTS idx_identity_announcements_audience
    ON identity.system_announcements (audience, status);
CREATE INDEX IF NOT EXISTS idx_identity_announcements_published_by
    ON identity.system_announcements (published_by, published_at);

INSERT INTO identity.system_configurations (
    id,
    config_key,
    display_name,
    description,
    config_value,
    value_type,
    sensitive,
    editable,
    requires_confirmation,
    created_at,
    updated_at
)
VALUES
    (gen_random_uuid(), 'announcement.policy.enabled', 'Announcement Policy Enabled', 'Controls whether system announcements can be broadcast.', 'false', 'BOOLEAN', false, true, true, now(), now()),
    (gen_random_uuid(), 'maintenance.policy.enabled', 'Maintenance Policy Enabled', 'Controls whether maintenance mode can be changed.', 'false', 'BOOLEAN', false, true, true, now(), now()),
    (gen_random_uuid(), 'maintenance.mode.enabled', 'Maintenance Mode Enabled', 'Current operational maintenance status.', 'false', 'BOOLEAN', false, true, true, now(), now()),
    (gen_random_uuid(), 'maintenance.message', 'Maintenance Message', 'Message displayed with maintenance status.', 'LifeBalance is operating normally.', 'STRING', false, true, false, now(), now())
ON CONFLICT (config_key) DO NOTHING;

WITH permission_seed(code, name, module, description) AS (
    VALUES
        ('support-ticket:create', 'Create Support Tickets', 'administration_support', 'Allows creating support tickets.'),
        ('support-ticket:read', 'Read Support Tickets', 'administration_support', 'Allows reading support tickets within policy scope.'),
        ('support-ticket:update', 'Update Support Tickets', 'administration_support', 'Allows updating support ticket workflow.'),
        ('support-ticket:assign', 'Assign Support Tickets', 'administration_support', 'Allows assigning support tickets to staff.'),
        ('support-ticket:resolve', 'Resolve Support Tickets', 'administration_support', 'Allows resolving, closing, and reopening support tickets.'),
        ('activity-log:read', 'Read Activity Logs', 'administration_support', 'Allows reading activity logs.'),
        ('configuration:read', 'Read System Configuration', 'administration_support', 'Allows reading system configuration.'),
        ('configuration:update', 'Update System Configuration', 'administration_support', 'Allows updating system configuration.'),
        ('administration-dashboard:read', 'Read Administration Dashboard', 'administration_support', 'Allows reading administration dashboard.'),
        ('announcement:create', 'Create Announcements', 'administration_support', 'Allows broadcasting system announcements when policy permits.'),
        ('announcement:read', 'Read Announcements', 'administration_support', 'Allows reading system announcements.'),
        ('maintenance:read', 'Read Maintenance Status', 'administration_support', 'Allows reading maintenance status.')
),
selected_existing_permissions AS (
    SELECT DISTINCT ON (lower(permission.code))
        permission.id,
        lower(permission.code) AS normalized_code
    FROM identity.permissions permission
    JOIN permission_seed seed
        ON lower(permission.code) = lower(seed.code)
    ORDER BY
        lower(permission.code),
        (permission.deleted_at IS NULL) DESC,
        permission.created_at ASC
),
updated_permissions AS (
    UPDATE identity.permissions permission
    SET
        code = seed.code,
        name = seed.name,
        module = seed.module,
        description = seed.description,
        is_system = true,
        updated_at = now(),
        deleted_at = NULL
    FROM permission_seed seed
    JOIN selected_existing_permissions existing_permission
        ON existing_permission.normalized_code = lower(seed.code)
    WHERE permission.id = existing_permission.id
    RETURNING permission.id
)
INSERT INTO identity.permissions (
    id,
    code,
    name,
    module,
    description,
    is_system,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    gen_random_uuid(),
    seed.code,
    seed.name,
    seed.module,
    seed.description,
    true,
    now(),
    now(),
    NULL
FROM permission_seed seed
WHERE NOT EXISTS (
    SELECT 1
    FROM selected_existing_permissions existing_permission
    WHERE existing_permission.normalized_code = lower(seed.code)
);

WITH admin_permissions AS (
    SELECT permission.id
    FROM identity.permissions permission
    WHERE permission.code IN (
        'support-ticket:create',
        'support-ticket:read',
        'support-ticket:update',
        'support-ticket:assign',
        'support-ticket:resolve',
        'activity-log:read',
        'configuration:read',
        'configuration:update',
        'administration-dashboard:read',
        'announcement:create',
        'announcement:read',
        'maintenance:read'
    )
      AND permission.deleted_at IS NULL
),
admin_roles AS (
    SELECT role.id
    FROM identity.roles role
    WHERE lower(role.code) = 'admin'
      AND role.deleted_at IS NULL
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT admin_roles.id, admin_permissions.id, now()
FROM admin_roles
CROSS JOIN admin_permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;

WITH manager_permissions AS (
    SELECT permission.id
    FROM identity.permissions permission
    WHERE permission.code IN (
        'support-ticket:create',
        'support-ticket:read',
        'support-ticket:update',
        'support-ticket:assign',
        'support-ticket:resolve',
        'activity-log:read',
        'administration-dashboard:read',
        'announcement:read',
        'maintenance:read'
    )
      AND permission.deleted_at IS NULL
),
manager_roles AS (
    SELECT role.id
    FROM identity.roles role
    WHERE lower(role.code) = 'manager'
      AND role.deleted_at IS NULL
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT manager_roles.id, manager_permissions.id, now()
FROM manager_roles
CROSS JOIN manager_permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;

WITH user_permissions AS (
    SELECT permission.id
    FROM identity.permissions permission
    WHERE permission.code IN (
        'support-ticket:create',
        'support-ticket:read',
        'announcement:read',
        'maintenance:read'
    )
      AND permission.deleted_at IS NULL
),
user_roles AS (
    SELECT role.id
    FROM identity.roles role
    WHERE lower(role.code) = 'user'
      AND role.deleted_at IS NULL
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT user_roles.id, user_permissions.id, now()
FROM user_roles
CROSS JOIN user_permissions
ON CONFLICT (role_id, permission_id) DO NOTHING;
