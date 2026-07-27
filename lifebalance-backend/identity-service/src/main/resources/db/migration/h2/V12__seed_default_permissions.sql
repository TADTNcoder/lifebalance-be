-- Seed application-level default permissions and role mappings for RBAC.
-- H2 test migration equivalent of the PostgreSQL default permission seed.

MERGE INTO identity.permissions (
    code,
    name,
    module,
    description,
    is_system,
    updated_at,
    deleted_at
)
KEY (code)
VALUES
    ('user:read', 'Read Users', 'user_management', 'Allows reading user accounts and profiles.', true, CURRENT_TIMESTAMP, NULL),
    ('user:create', 'Create Users', 'user_management', 'Allows creating user accounts.', true, CURRENT_TIMESTAMP, NULL),
    ('user:update', 'Update Users', 'user_management', 'Allows updating user accounts.', true, CURRENT_TIMESTAMP, NULL),
    ('user:delete', 'Delete Users', 'user_management', 'Allows deleting user accounts.', true, CURRENT_TIMESTAMP, NULL),
    ('user:lock', 'Lock Users', 'user_management', 'Allows locking user accounts.', true, CURRENT_TIMESTAMP, NULL),
    ('user:unlock', 'Unlock Users', 'user_management', 'Allows unlocking user accounts.', true, CURRENT_TIMESTAMP, NULL),
    ('role:read', 'Read Roles', 'role_management', 'Allows reading roles.', true, CURRENT_TIMESTAMP, NULL),
    ('role:create', 'Create Roles', 'role_management', 'Allows creating roles.', true, CURRENT_TIMESTAMP, NULL),
    ('role:update', 'Update Roles', 'role_management', 'Allows updating roles.', true, CURRENT_TIMESTAMP, NULL),
    ('role:delete', 'Delete Roles', 'role_management', 'Allows deleting roles.', true, CURRENT_TIMESTAMP, NULL),
    ('role:assign', 'Assign Roles', 'role_management', 'Allows assigning roles to users.', true, CURRENT_TIMESTAMP, NULL),
    ('permission:read', 'Read Permissions', 'permission_management', 'Allows reading permissions.', true, CURRENT_TIMESTAMP, NULL),
    ('permission:create', 'Create Permissions', 'permission_management', 'Allows creating permissions.', true, CURRENT_TIMESTAMP, NULL),
    ('permission:update', 'Update Permissions', 'permission_management', 'Allows updating permissions.', true, CURRENT_TIMESTAMP, NULL),
    ('audit:read', 'Read Audit Logs', 'audit_log', 'Allows reading audit logs.', true, CURRENT_TIMESTAMP, NULL),
    ('audit:export', 'Export Audit Logs', 'audit_log', 'Allows exporting audit logs.', true, CURRENT_TIMESTAMP, NULL),
    ('profile:read', 'Read Own Profile', 'profile', 'Allows reading the current user profile.', true, CURRENT_TIMESTAMP, NULL),
    ('profile:update', 'Update Own Profile', 'profile', 'Allows updating the current user profile.', true, CURRENT_TIMESTAMP, NULL);

INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT
    role.id,
    permission.id,
    CURRENT_TIMESTAMP
FROM identity.roles role
CROSS JOIN identity.permissions permission
WHERE lower(role.code) = 'admin'
  AND role.deleted_at IS NULL
  AND permission.code IN (
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
  AND NOT EXISTS (
      SELECT 1
      FROM identity.role_permissions role_permission
      WHERE role_permission.role_id = role.id
        AND role_permission.permission_id = permission.id
  );

INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT
    role.id,
    permission.id,
    CURRENT_TIMESTAMP
FROM identity.roles role
CROSS JOIN identity.permissions permission
WHERE lower(role.code) = 'user'
  AND role.deleted_at IS NULL
  AND permission.code IN ('profile:read', 'profile:update')
  AND permission.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM identity.role_permissions role_permission
      WHERE role_permission.role_id = role.id
        AND role_permission.permission_id = permission.id
  );
