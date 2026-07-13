-- Seed application-level default roles for the LifeBalance identity module.
-- This migration is idempotent and restores a system role if it was soft-deleted.

WITH default_roles(code, name, description) AS (
    VALUES
        (
            'ADMIN',
            'Administrator',
            'Application administrator with access to system management features.'
        ),
        (
            'MANAGER',
            'Manager',
            'Application manager with access to operational management features.'
        ),
        (
            'USER',
            'User',
            'Default role assigned to a standard LifeBalance user.'
        )
),
selected_existing_roles AS (
    SELECT DISTINCT ON (lower(role.code))
        role.id,
        lower(role.code) AS normalized_code
    FROM identity.roles role
    JOIN default_roles default_role
        ON lower(role.code) = lower(default_role.code)
    ORDER BY
        lower(role.code),
        (role.deleted_at IS NULL) DESC,
        role.created_at ASC
),
updated_roles AS (
    UPDATE identity.roles role
    SET
        code = default_role.code,
        name = default_role.name,
        description = default_role.description,
        is_system = true,
        updated_at = now(),
        deleted_at = NULL
    FROM default_roles default_role
    JOIN selected_existing_roles existing_role
        ON existing_role.normalized_code = lower(default_role.code)
    WHERE role.id = existing_role.id
    RETURNING role.id
)
INSERT INTO identity.roles (
    id,
    code,
    name,
    description,
    is_system,
    created_at,
    updated_at,
    deleted_at
)
SELECT
    gen_random_uuid(),
    default_role.code,
    default_role.name,
    default_role.description,
    true,
    now(),
    now(),
    NULL
FROM default_roles default_role
WHERE NOT EXISTS (
    SELECT 1
    FROM identity.roles role
    WHERE lower(role.code) = lower(default_role.code)
);
