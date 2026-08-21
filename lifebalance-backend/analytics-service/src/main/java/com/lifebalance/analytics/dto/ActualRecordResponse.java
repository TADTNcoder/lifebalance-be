package com.lifebalance.analytics.dto;

import com.lifebalance.analytics.domain.ActualRecordStatus;
import com.lifebalance.analytics.domain.ActualRecordType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record ActualRecordResponse(
        UUID id,
        UUID ownerId,
        UUID actorId,
        UUID taskId,
        UUID capitalCycleId,
        UUID categoryId,
        Set<UUID> tagIds,
        ActualRecordType recordType,
        ActualRecordStatus status,
        Integer actualMinutes,
        BigDecimal actualCost,
        String currencyCode,
        LocalDate actualDate,
        OffsetDateTime recordedAt,
        String note,
        String source,
        OffsetDateTime archivedAt,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
