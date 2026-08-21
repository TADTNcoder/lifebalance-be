package com.lifebalance.analytics.dto;

import com.lifebalance.analytics.domain.ActualRecordType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record UpdateActualRecordRequest(
        ActualRecordType recordType,
        UUID taskId,
        UUID capitalCycleId,
        UUID categoryId,
        Set<UUID> tagIds,
        @PositiveOrZero Integer actualMinutes,
        @DecimalMin(value = "0.0000") BigDecimal actualCost,
        @Size(min = 3, max = 3) String currencyCode,
        LocalDate actualDate,
        @Size(max = 1000) String note,
        @Size(max = 64) String source,
        @Size(max = 1000) String reason
) {
}
