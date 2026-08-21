package com.lifebalance.timeline.dto;

import jakarta.validation.constraints.Size;

public record CancelTimelinePlacementRequest(
        @Size(max = 500) String reason
) {
}
