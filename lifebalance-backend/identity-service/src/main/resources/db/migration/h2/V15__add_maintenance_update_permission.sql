MERGE INTO identity.permissions (
    code,
    name,
    module,
    description,
    is_system,
    created_at,
    deleted_at
) KEY (code)
VALUES (
    'maintenance:update',
    'Update Maintenance Status',
    'administration_support',
    'Allows updating maintenance mode, message, and window.',
    true,
    CURRENT_TIMESTAMP,
    NULL
);

INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT role.id, permission.id, CURRENT_TIMESTAMP
FROM identity.roles role
CROSS JOIN identity.permissions permission
WHERE lower(role.code) = 'admin'
  AND role.deleted_at IS NULL
  AND permission.code = 'maintenance:update'
  AND permission.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM identity.role_permissions role_permission
      WHERE role_permission.role_id = role.id
        AND role_permission.permission_id = permission.id
  );
