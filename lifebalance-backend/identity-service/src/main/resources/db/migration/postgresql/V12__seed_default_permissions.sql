-- Seed application-level default permissions and role mappings for RBAC.
-- This migration is idempotent and restores system permissions if they were soft-deleted.

WITH default_permissions(code, name, module, description) AS (
    VALUES
        ('user:read', 'Read Users', 'user_management', 'Allows reading user accounts and profiles.'),
        ('user:create', 'Create Users', 'user_management', 'Allows creating user accounts.'),
        ('user:update', 'Update Users', 'user_management', 'Allows updating user accounts.'),
        ('user:delete', 'Delete Users', 'user_management', 'Allows deleting user accounts.'),
        ('user:lock', 'Lock Users', 'user_management', 'Allows locking user accounts.'),
        ('user:unlock', 'Unlock Users', 'user_management', 'Allows unlocking user accounts.'),
        ('role:read', 'Read Roles', 'role_management', 'Allows reading roles.'),
        ('role:create', 'Create Roles', 'role_management', 'Allows creating roles.'),
        ('role:update', 'Update Roles', 'role_management', 'Allows updating roles.'),
        ('role:delete', 'Delete Roles', 'role_management', 'Allows deleting roles.'),
        ('role:assign', 'Assign Roles', 'role_management', 'Allows assigning roles to users.'),
        ('permission:read', 'Read Permissions', 'permission_management', 'Allows reading permissions.'),
        ('permission:create', 'Create Permissions', 'permission_management', 'Allows creating permissions.'),
        ('permission:update', 'Update Permissions', 'permission_management', 'Allows updating permissions.'),
        ('audit:read', 'Read Audit Logs', 'audit_log', 'Allows reading audit logs.'),
        ('audit:export', 'Export Audit Logs', 'audit_log', 'Allows exporting audit logs.'),
        ('profile:read', 'Read Own Profile', 'profile', 'Allows reading the current user profile.'),
        ('profile:update', 'Update Own Profile', 'profile', 'Allows updating the current user profile.')
),
selected_existing_permissions AS (
    SELECT DISTINCT ON (lower(permission.code))
        permission.id,
        lower(permission.code) AS normalized_code
    FROM identity.permissions permission
    JOIN default_permissions default_permission
        ON lower(permission.code) = lower(default_permission.code)
    ORDER BY
        lower(permission.code),
        (permission.deleted_at IS NULL) DESC,
        permission.created_at ASC
),
updated_permissions AS (
    UPDATE identity.permissions permission
    SET
        code = default_permission.code,
        name = default_permission.name,
        module = default_permission.module,
        description = default_permission.description,
        is_system = true,
        updated_at = now(),
        deleted_at = NULL
    FROM default_permissions default_permission
    JOIN selected_existing_permissions existing_permission
        ON existing_permission.normalized_code = lower(default_permission.code)
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
    default_permission.code,
    default_permission.name,
    default_permission.module,
    default_permission.description,
    true,
    now(),
    now(),
    NULL
FROM default_permissions default_permission
WHERE NOT EXISTS (
    SELECT 1
    FROM identity.permissions permission
    WHERE lower(permission.code) = lower(default_permission.code)
);

WITH admin_role AS (
    SELECT role.id
    FROM identity.roles role
    WHERE lower(role.code) = 'admin'
      AND role.deleted_at IS NULL
    LIMIT 1
),
default_permissions AS (
    SELECT permission.id
    FROM identity.permissions permission
    WHERE permission.code IN (
        'user:read',
        'user:create',
        'user:update',
        'user:delete',
        'user:lock',
        'user:unlock',
        'role:read',
        'role:create',
        'role:update',
        'role:delete',
        'role:assign',
        'permission:read',
        'permission:create',
        'permission:update',
        'audit:read',
        'audit:export',
        'profile:read',
        'profile:update'
    )
      AND permission.deleted_at IS NULL
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT
    admin_role.id,
    default_permission.id,
    now()
FROM admin_role
CROSS JOIN default_permissions default_permission
ON CONFLICT (role_id, permission_id) DO NOTHING;

WITH user_role AS (
    SELECT role.id
    FROM identity.roles role
    WHERE lower(role.code) = 'user'
      AND role.deleted_at IS NULL
    LIMIT 1
),
profile_permissions AS (
    SELECT permission.id
    FROM identity.permissions permission
    WHERE permission.code IN ('profile:read', 'profile:update')
      AND permission.deleted_at IS NULL
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT
    user_role.id,
    profile_permission.id,
    now()
FROM user_role
CROSS JOIN profile_permissions profile_permission
ON CONFLICT (role_id, permission_id) DO NOTHING;
