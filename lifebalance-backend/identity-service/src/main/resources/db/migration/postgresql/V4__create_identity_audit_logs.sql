
CREATE TABLE identity.audit_logs
(
    id UUID PRIMARY KEY,

    user_id UUID,

    keycloak_id VARCHAR(255),
    
    service_name VARCHAR(100) NOT NULL,

    action VARCHAR(50) NOT NULL,

    status VARCHAR(50) NOT NULL,

    ip_address VARCHAR(100),

    user_agent TEXT,

    details TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user
        FOREIGN KEY (user_id)
        REFERENCES identity.users(id)
);
CREATE INDEX idx_audit_user
ON identity.audit_logs(user_id);

CREATE INDEX idx_audit_created_at
ON identity.audit_logs(created_at);

CREATE INDEX idx_audit_action
ON identity.audit_logs(action);