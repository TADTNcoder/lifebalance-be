package com.lifebalance.timeline.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ScheduleTimelinePlacementRequest(
        @NotNull UUID taskId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @Size(max = 64) String timezone,
        Boolean conflictConfirmed,
        @Size(max = 500) String conflictReason,
        @Size(max = 500) String reason
) {
}
