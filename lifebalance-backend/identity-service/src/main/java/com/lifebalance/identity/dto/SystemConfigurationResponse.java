package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.SystemConfigurationValueType;

import lombok.Builder;

@Builder
public record SystemConfigurationResponse(
        UUID id,
        String configKey,
        String displayName,
        String description,
        String value,
        SystemConfigurationValueType valueType,
        boolean sensitive,
        boolean editable,
        boolean requiresConfirmation,
        UUID updatedBy,
        String lastChangeReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
