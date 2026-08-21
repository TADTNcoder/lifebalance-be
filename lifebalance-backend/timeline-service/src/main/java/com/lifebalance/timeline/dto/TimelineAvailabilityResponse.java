package com.lifebalance.timeline.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TimelineAvailabilityResponse(
        UUID ownerId,
        OffsetDateTime from,
        OffsetDateTime to,
        boolean available,
        List<TimelineConflictResponse> conflicts
) {
}
