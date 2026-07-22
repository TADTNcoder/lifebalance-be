
CREATE TABLE identity.audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID,
    keycloak_id VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    ip_address VARCHAR(100),
    user_agent TEXT,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id)
        REFERENCES identity.users(id)
);

CREATE INDEX idx_audit_user ON identity.audit_logs(user_id);
CREATE INDEX idx_audit_created_at ON identity.audit_logs(created_at);
CREATE INDEX idx_audit_action ON identity.audit_logs(action);
CREATE INDEX idx_audit_deleted_at ON identity.audit_logs(deleted_at);
