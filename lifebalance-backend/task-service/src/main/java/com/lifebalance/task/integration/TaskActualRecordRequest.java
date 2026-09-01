package com.lifebalance.task.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

record TaskActualRecordRequest(
        String recordType,
        UUID taskId,
        UUID capitalCycleId,
        UUID categoryId,
        Set<UUID> tagIds,
        Integer actualMinutes,
        BigDecimal actualCost,
        String currencyCode,
        LocalDate actualDate,
        String note,
        String source,
        TaskEvaluationBaselineRequest evaluationBaseline
) {

    TaskActualRecordRequest(
            String recordType,
            UUID taskId,
            UUID capitalCycleId,
            UUID categoryId,
            Set<UUID> tagIds,
            Integer actualMinutes,
            BigDecimal actualCost,
            String currencyCode,
            LocalDate actualDate,
            String note,
            String source
    ) {
        this(
                recordType,
                taskId,
                capitalCycleId,
                categoryId,
                tagIds,
                actualMinutes,
                actualCost,
                currencyCode,
                actualDate,
                note,
                source,
                null
        );
    }
}
