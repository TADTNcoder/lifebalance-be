package com.lifebalance.task.dto.request;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.UUID;

import com.lifebalance.task.model.enums.PriorityLevel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
public class CreateTaskRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String name;

    private String description;

    private PriorityLevel priority;

    private LocalDate deadline;

    @Min(0)
    private Integer estimatedMinutes;

    @PositiveOrZero
    private BigDecimal estimatedCost;

    private UUID categoryId;
}
