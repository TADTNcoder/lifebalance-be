-- Add the missing delete permission for permission management.

WITH permission_seed(code, name, module, description) AS (
    VALUES (
        'permission:delete',
        'Delete Permissions',
        'permission_management',
        'Allows deleting permissions.'
    )
),
selected_existing_permission AS (
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
updated_permission AS (
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
    JOIN selected_existing_permission existing_permission
        ON existing_permission.normalized_code = lower(seed.code)
    WHERE permission.id = existing_permission.id
    RETURNING permission.id
),
inserted_permission AS (
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
        FROM selected_existing_permission existing_permission
        WHERE existing_permission.normalized_code = lower(seed.code)
    )
    RETURNING id
),
upserted_permission AS (
    SELECT id FROM updated_permission
    UNION ALL
    SELECT id FROM inserted_permission
),
admin_role AS (
    SELECT role.id
    FROM identity.roles role
    WHERE lower(role.code) = 'admin'
      AND role.deleted_at IS NULL
    LIMIT 1
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT
    admin_role.id,
    upserted_permission.id,
    now()
FROM admin_role
CROSS JOIN upserted_permission
ON CONFLICT (role_id, permission_id) DO NOTHING;
