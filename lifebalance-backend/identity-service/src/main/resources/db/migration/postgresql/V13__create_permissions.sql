CREATE TABLE identity.permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(150) NOT NULL,
    name VARCHAR(255) NOT NULL,
    module VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);



CREATE UNIQUE INDEX uq_identity_permissions_code_active
    ON identity.permissions (lower(code))
    WHERE deleted_at IS NULL;

CREATE INDEX idx_identity_permissions_module ON identity.permissions (module);
CREATE INDEX idx_identity_permissions_deleted_at ON identity.permissions (deleted_at);
