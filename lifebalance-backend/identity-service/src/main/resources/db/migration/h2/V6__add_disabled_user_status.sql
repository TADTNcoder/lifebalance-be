ALTER TABLE identity.users
    DROP CONSTRAINT IF EXISTS chk_identity_users_status;

ALTER TABLE identity.users
    ADD CONSTRAINT chk_identity_users_status
        CHECK (status IN ('ACTIVE', 'DISABLED', 'INACTIVE', 'SUSPENDED', 'DELETED'));
