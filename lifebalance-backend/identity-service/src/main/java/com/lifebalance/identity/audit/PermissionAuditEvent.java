package com.lifebalance.identity.audit;

import java.util.UUID;

import com.lifebalance.identity.model.enums.AuditAction;

public record PermissionAuditEvent(
        AuditAction action,
        UUID permissionId,
        AuditActor actor,
        AuditRequestMetadata requestMetadata,
        String oldValue,
        String newValue,
        String details
) {
}
