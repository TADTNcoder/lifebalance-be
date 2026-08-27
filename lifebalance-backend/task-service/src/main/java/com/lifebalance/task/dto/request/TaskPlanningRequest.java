package com.lifebalance.task.dto.request;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.validation.ValidPlanningWindow;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ValidPlanningWindow
public class TaskPlanningRequest implements PlanningWindowRequest {

    private PriorityLevel priority;

    private LocalDate deadline;

    private OffsetDateTime plannedStartAt;

    private OffsetDateTime plannedEndAt;

    @Positive
    private Integer estimatedMinutes;

    @PositiveOrZero
    private BigDecimal estimatedCost;

    private UUID categoryId;

    @Size(max = 500)
    private String reason;
}
