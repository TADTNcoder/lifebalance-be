ALTER TABLE identity.users
    DROP CONSTRAINT IF EXISTS chk_identity_users_status;

ALTER TABLE identity.users
    ADD CONSTRAINT chk_identity_users_status
        CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED', 'INACTIVE', 'SUSPENDED', 'DELETED'));

ALTER TABLE identity.users
    ADD COLUMN IF NOT EXISTS lock_reason TEXT,
    ADD COLUMN IF NOT EXISTS locked_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS locked_until TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS locked_by_keycloak_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS token_valid_after TIMESTAMPTZ;
