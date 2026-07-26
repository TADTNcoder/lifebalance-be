ALTER TABLE identity.audit_logs
    ADD CONSTRAINT chk_identity_audit_logs_entity_name
        CHECK (entity_name IN (
            'AUTHENTICATION',
            'USER',
            'ROLE',
            'PERMISSION',
            'USER_ROLE',
            'ROLE_PERMISSION'
        ));

ALTER TABLE identity.audit_logs
    ADD CONSTRAINT chk_identity_audit_logs_action
        CHECK (action IN (
            'LOGIN',
            'LOGOUT',
            'CREATE_ROLE',
            'UPDATE_ROLE',
            'DELETE_ROLE',
            'ASSIGN_PERMISSION',
            'ASSIGN_ROLE_PERMISSIONS',
            'REVOKE_PERMISSION',
            'CREATE_PERMISSION',
            'UPDATE_PERMISSION',
            'DELETE_PERMISSION',
            'UPDATE_USER',
            'ACTIVATE_USER',
            'DISABLE_USER',
            'LOCK_USER',
            'UNLOCK_USER',
            'DELETE_USER',
            'ASSIGN_ROLE',
            'REVOKE_ROLE'
        ));

ALTER TABLE identity.audit_logs
    ADD CONSTRAINT chk_identity_audit_logs_status
        CHECK (status IN ('SUCCESS', 'FAILED'));
