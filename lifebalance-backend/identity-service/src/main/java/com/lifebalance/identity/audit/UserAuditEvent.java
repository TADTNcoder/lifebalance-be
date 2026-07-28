package com.lifebalance.identity.audit;

import java.util.UUID;

import com.lifebalance.identity.model.enums.AuditAction;

public record UserAuditEvent(
        AuditAction action,
        UUID userId,
        String keycloakId,
        AuditActor actor,
        AuditRequestMetadata requestMetadata,
        String oldValue,
        String newValue,
        String details
) {
}
