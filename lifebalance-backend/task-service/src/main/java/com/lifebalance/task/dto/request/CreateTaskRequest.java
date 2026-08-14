package com.lifebalance.task.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.lifebalance.task.model.enums.PriorityLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank
    private String name;

    private String description;

    private PriorityLevel priority;

    private LocalDate deadline;

    @PositiveOrZero
    private Integer estimatedMinutes;

    @PositiveOrZero
    private BigDecimal estimatedCost;

    private UUID categoryId;
}