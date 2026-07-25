ALTER TABLE identity.permissions
    ADD COLUMN is_system BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX idx_identity_permissions_is_system
    ON identity.permissions (is_system);
