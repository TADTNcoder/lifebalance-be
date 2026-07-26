ALTER TABLE identity.audit_logs
    ADD COLUMN entity_name VARCHAR(50),
    ADD COLUMN entity_id VARCHAR(255),
    ADD COLUMN actor_id UUID,
    ADD COLUMN actor_keycloak_id VARCHAR(255),
    ADD COLUMN actor_username VARCHAR(100),
    ADD COLUMN old_value TEXT,
    ADD COLUMN new_value TEXT;

UPDATE identity.audit_logs
SET entity_name = 'AUTHENTICATION',
    entity_id = user_id::text,
    actor_id = user_id,
    actor_keycloak_id = keycloak_id
WHERE entity_name IS NULL;

ALTER TABLE identity.audit_logs
    ALTER COLUMN entity_name SET NOT NULL;

ALTER TABLE identity.audit_logs
    ADD CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_id)
        REFERENCES identity.users(id);

CREATE INDEX idx_identity_audit_logs_entity
    ON identity.audit_logs(entity_name, entity_id, created_at DESC);

CREATE INDEX idx_identity_audit_logs_actor
    ON identity.audit_logs(actor_id, created_at DESC);

CREATE INDEX idx_identity_audit_logs_action_created_at
    ON identity.audit_logs(action, created_at DESC);

CREATE INDEX idx_identity_audit_logs_status_created_at
    ON identity.audit_logs(status, created_at DESC);
