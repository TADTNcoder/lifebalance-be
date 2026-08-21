package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.ActivityCategory;

import lombok.Builder;

@Builder
public record ActivityLogResponse(
        UUID id,
        UUID actorId,
        String actorUsername,
        ActivityCategory category,
        String action,
        String entityType,
        String entityId,
        String summary,
        String details,
        OffsetDateTime occurredAt,
        OffsetDateTime createdAt
) {
}
