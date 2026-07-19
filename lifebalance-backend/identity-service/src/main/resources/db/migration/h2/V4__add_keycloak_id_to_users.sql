ALTER TABLE identity.users
    ADD COLUMN IF NOT EXISTS keycloak_id VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_users_keycloak_id
    ON identity.users (keycloak_id);
