CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.users (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    username VARCHAR(100),
    display_name VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_identity_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE UNIQUE INDEX uq_identity_users_email_active ON identity.users (email);
CREATE UNIQUE INDEX uq_identity_users_username_active ON identity.users (username);
CREATE INDEX idx_identity_users_deleted_at ON identity.users (deleted_at);
