package com.lifebalance.timeline.dto;

import com.lifebalance.timeline.domain.TimelineTaskStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TimelineTaskResponse(
        UUID taskId,
        UUID ownerId,
        String title,
        TimelineTaskStatus taskStatus,
        boolean hasTimeCapital,
        Integer estimatedMinutes,
        LocalDate deadline,
        UUID capitalCycleId,
        OffsetDateTime cycleStartAt,
        OffsetDateTime cycleEndAt,
        OffsetDateTime scheduledStartAt,
        OffsetDateTime scheduledEndAt,
        boolean timelineEligible,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
