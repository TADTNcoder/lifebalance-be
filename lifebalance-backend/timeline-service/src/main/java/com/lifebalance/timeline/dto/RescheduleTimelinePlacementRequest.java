package com.lifebalance.timeline.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record RescheduleTimelinePlacementRequest(
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @Size(max = 64) String timezone,
        Boolean conflictConfirmed,
        @Size(max = 500) String conflictReason,
        @Size(max = 500) String reason
) {
}
