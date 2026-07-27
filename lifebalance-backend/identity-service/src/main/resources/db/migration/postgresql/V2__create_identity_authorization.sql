CREATE TABLE identity.roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);


CREATE TABLE identity.user_roles (
    user_id UUID NOT NULL REFERENCES identity.users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES identity.roles(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    assigned_by UUID REFERENCES identity.users(id) ON DELETE SET NULL,
    PRIMARY KEY (user_id, role_id)
);



CREATE UNIQUE INDEX uq_identity_roles_code_active
    ON identity.roles (lower(code))
    WHERE deleted_at IS NULL;



CREATE INDEX idx_identity_roles_deleted_at ON identity.roles (deleted_at);

CREATE INDEX idx_identity_user_roles_role_id ON identity.user_roles (role_id);
CREATE INDEX idx_identity_user_roles_assigned_by ON identity.user_roles (assigned_by);

