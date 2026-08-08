package com.lifebalance.task.dto.request;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskRequest {

    @NotBlank
    private String name;

    private String description;

    private PriorityLevel priority;

    private LocalDate deadline;

    @Min(0)
    @Max(100)
    private Integer progress;

    @Min(0)
    private Integer estimatedMinutes;

    @PositiveOrZero
    private BigDecimal estimatedCost;

    private UUID categoryId;

    private TaskStatus status;
}
