package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;

import lombok.Builder;

@Builder
public record AdministrationAuditLogResponse(
        UUID id,
        AuditEntityName entityName,
        String entityId,
        UUID actorId,
        String actorUsername,
        UUID userId,
        AuditAction action,
        AuditStatus status,
        String ipAddress,
        String userAgent,
        String oldValue,
        String newValue,
        String details,
        OffsetDateTime createdAt
) {
}
