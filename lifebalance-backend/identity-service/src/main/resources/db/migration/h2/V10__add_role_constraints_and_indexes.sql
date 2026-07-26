CREATE UNIQUE INDEX uq_identity_roles_name_active
    ON identity.roles (name);

CREATE INDEX idx_identity_roles_is_system_code_active
    ON identity.roles (is_system, code);
