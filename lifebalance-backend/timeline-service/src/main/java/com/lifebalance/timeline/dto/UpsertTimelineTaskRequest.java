package com.lifebalance.timeline.dto;

import com.lifebalance.timeline.domain.TimelineTaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpsertTimelineTaskRequest(
        @NotNull UUID taskId,
        @NotBlank @Size(max = 255) String title,
        @NotNull TimelineTaskStatus taskStatus,
        Boolean hasTimeCapital,
        @Positive Integer estimatedMinutes,
        LocalDate deadline,
        UUID capitalCycleId,
        OffsetDateTime cycleStartAt,
        OffsetDateTime cycleEndAt,
        OffsetDateTime scheduledStartAt,
        OffsetDateTime scheduledEndAt
) {
}
