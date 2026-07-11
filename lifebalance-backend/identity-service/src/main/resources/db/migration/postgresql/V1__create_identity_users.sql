CREATE SCHEMA IF NOT EXISTS identity;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type t
        JOIN pg_namespace n ON n.oid = t.typnamespace
        WHERE t.typname = 'account_status'
          AND n.nspname = 'identity'
    ) THEN
        CREATE TYPE identity.account_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED');
    END IF;
END
$$;

CREATE TABLE identity.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) UNIQUE,
    display_name VARCHAR(255),
    status identity.account_status NOT NULL DEFAULT 'ACTIVE',
    registered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ
);

CREATE INDEX idx_identity_users_email ON identity.users (email);
