-- Seed application-level default roles for the LifeBalance identity module.
-- H2 test migration equivalent of the PostgreSQL default role seed.

MERGE INTO identity.roles (
    code,
    name,
    description,
    is_system,
    updated_at,
    deleted_at
)
KEY (code)
VALUES
    (
        'ADMIN',
        'Administrator',
        'Application administrator with access to system management features.',
        true,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'MANAGER',
        'Manager',
        'Application manager with access to operational management features.',
        true,
        CURRENT_TIMESTAMP,
        NULL
    ),
    (
        'USER',
        'User',
        'Default role assigned to a standard LifeBalance user.',
        true,
        CURRENT_TIMESTAMP,
        NULL
    );
