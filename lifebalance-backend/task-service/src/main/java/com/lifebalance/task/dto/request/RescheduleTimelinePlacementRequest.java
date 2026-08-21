package com.lifebalance.task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class RescheduleTimelinePlacementRequest {

    @NotNull
    private OffsetDateTime startAt;

    @NotNull
    private OffsetDateTime endAt;

    @Size(max = 64)
    private String timezone;

    @Size(max = 500)
    private String reason;
}
