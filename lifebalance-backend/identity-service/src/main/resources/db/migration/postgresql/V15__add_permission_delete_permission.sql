WITH existing_permission AS (
    SELECT id
    FROM identity.permissions
    WHERE lower(code) = 'permission:delete'
    ORDER BY (deleted_at IS NULL) DESC, created_at ASC
    LIMIT 1
),
updated_permission AS (
    UPDATE identity.permissions permission
    SET code = 'permission:delete',
        name = 'Delete Permissions',
        module = 'permission_management',
        description = 'Allows deleting permissions.',
        is_system = true,
        deleted_at = NULL,
        updated_at = CURRENT_TIMESTAMP
    FROM existing_permission
    WHERE permission.id = existing_permission.id
    RETURNING permission.id
),
inserted_permission AS (
    INSERT INTO identity.permissions (
        code,
        name,
        module,
        description,
        is_system,
        created_at,
        updated_at
    )
    SELECT
        'permission:delete',
        'Delete Permissions',
        'permission_management',
        'Allows deleting permissions.',
        true,
        CURRENT_TIMESTAMP,
        NULL
    WHERE NOT EXISTS (SELECT 1 FROM existing_permission)
    RETURNING id
),
permission_delete AS (
    SELECT id FROM updated_permission
    UNION ALL
    SELECT id FROM inserted_permission
),
admin_role AS (
    SELECT id
    FROM identity.roles
    WHERE lower(code) = 'admin'
      AND deleted_at IS NULL
    LIMIT 1
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT admin_role.id, permission_delete.id, CURRENT_TIMESTAMP
FROM admin_role
CROSS JOIN permission_delete
ON CONFLICT (role_id, permission_id) DO NOTHING;
