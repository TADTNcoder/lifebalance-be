WITH permission_seed(code, name, module, description) AS (
    VALUES (
        'maintenance:update',
        'Update Maintenance Status',
        'administration_support',
        'Allows updating maintenance mode, message, and window.'
    )
),
selected_existing_permissions AS (
    SELECT DISTINCT ON (lower(permission.code))
           permission.id,
           lower(permission.code) AS normalized_code
    FROM identity.permissions permission
    JOIN permission_seed seed ON lower(permission.code) = lower(seed.code)
    ORDER BY lower(permission.code), permission.deleted_at IS NULL DESC, permission.created_at ASC
),
reactivated_permissions AS (
    UPDATE identity.permissions permission
        SET name = seed.name,
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
),
inserted_permissions AS (
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
    )
    RETURNING id
),
permission_ids AS (
    SELECT id FROM reactivated_permissions
    UNION ALL
    SELECT id FROM inserted_permissions
),
admin_roles AS (
    SELECT role.id
    FROM identity.roles role
    WHERE lower(role.code) = 'admin'
      AND role.deleted_at IS NULL
)
INSERT INTO identity.role_permissions (role_id, permission_id, granted_at)
SELECT admin_roles.id, permission_ids.id, now()
FROM admin_roles
CROSS JOIN permission_ids
ON CONFLICT (role_id, permission_id) DO NOTHING;
