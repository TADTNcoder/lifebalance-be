CREATE SCHEMA IF NOT EXISTS identity;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE identity.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    username VARCHAR(100),
    display_name VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT chk_identity_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE UNIQUE INDEX uq_identity_users_email_active
    ON identity.users (lower(email))
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_identity_users_username_active
    ON identity.users (lower(username))
    WHERE username IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX idx_identity_users_deleted_at ON identity.users (deleted_at);
