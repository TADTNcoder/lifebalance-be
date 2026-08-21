package com.lifebalance.task.dto.response;

import com.lifebalance.task.model.enums.TimelinePlacementStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class TimelinePlacementResponse {

    private UUID id;

    private UUID ownerId;

    private UUID userId;

    private UUID taskId;

    private String taskName;

    private OffsetDateTime startAt;

    private OffsetDateTime endAt;

    private String timezone;

    private String source;

    private TimelinePlacementStatus status;

    private String reason;

    private UUID createdBy;

    private UUID updatedBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
