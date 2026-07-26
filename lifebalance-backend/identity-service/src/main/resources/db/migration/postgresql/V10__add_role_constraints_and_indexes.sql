DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM identity.roles
        WHERE deleted_at IS NULL
        GROUP BY lower(name)
        HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot add uq_identity_roles_name_active because duplicate active role names exist';
    END IF;
END $$;

CREATE UNIQUE INDEX uq_identity_roles_name_active
    ON identity.roles (lower(name))
    WHERE deleted_at IS NULL;

CREATE INDEX idx_identity_roles_is_system_code_active
    ON identity.roles (is_system, code)
    WHERE deleted_at IS NULL;
