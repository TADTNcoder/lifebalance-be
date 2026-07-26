package com.lifebalance.identity.audit;

import java.util.UUID;

import com.lifebalance.identity.model.enums.AuditAction;

public record RoleAuditEvent(
        AuditAction action,
        UUID roleId,
        AuditActor actor,
        AuditRequestMetadata requestMetadata,
        String oldValue,
        String newValue,
        String details
) {
}
