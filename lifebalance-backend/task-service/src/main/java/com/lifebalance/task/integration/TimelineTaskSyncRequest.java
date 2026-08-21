package com.lifebalance.task.integration;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

record TimelineTaskSyncRequest(
        UUID taskId,
        String title,
        String taskStatus,
        Boolean hasTimeCapital,
        Integer estimatedMinutes,
        LocalDate deadline,
        UUID capitalCycleId,
        OffsetDateTime cycleStartAt,
        OffsetDateTime cycleEndAt,
        OffsetDateTime scheduledStartAt,
        OffsetDateTime scheduledEndAt
) {
}
