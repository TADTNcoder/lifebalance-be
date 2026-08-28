package com.lifebalance.task.dto.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.UUID;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

import lombok.*;

@Getter
@Setter
public class TaskResponse {

    private UUID id;

    private UUID ownerId;

    private UUID userId;

    private String name;

    private String description;

    private String note;

    private String currency;

    private TaskStatus status;

    private PriorityLevel priority;

    private LocalDate deadline;

    private OffsetDateTime plannedStartAt;

    private OffsetDateTime plannedEndAt;

    private OffsetDateTime scheduledStartAt;

    private OffsetDateTime scheduledEndAt;

    private OffsetDateTime completedAt;

    private OffsetDateTime cancelledAt;

    private OffsetDateTime archivedAt;

    private Integer progress;

    private Integer estimatedMinutes;

    private BigDecimal estimatedCost;

    private UUID categoryId;

    private String categoryName;

    private UUID createdBy;

    private UUID updatedBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
