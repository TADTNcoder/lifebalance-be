package com.lifebalance.identity.service;

import java.util.UUID;

import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;

public record AuditLogCommand(
        AuditEntityName entityName,
        String entityId,
        UUID actorId,
        String actorKeycloakId,
        String actorUsername,
        UUID userId,
        String keycloakId,
        AuditAction action,
        AuditStatus status,
        String ipAddress,
        String userAgent,
        String oldValue,
        String newValue,
        String details
) {
}
