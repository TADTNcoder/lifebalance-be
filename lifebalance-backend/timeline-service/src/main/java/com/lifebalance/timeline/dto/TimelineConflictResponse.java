package com.lifebalance.timeline.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimelineConflictResponse(
        UUID placementId,
        UUID taskId,
        String taskTitle,
        OffsetDateTime startAt,
        OffsetDateTime endAt
) {
}
