package com.lifebalance.task.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.validation.ValidPlanningWindow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ValidPlanningWindow
public class CreateTaskRequest implements PlanningWindowRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 2000)
    private String note;

    @Size(max = 3)
    private String currency;

    private PriorityLevel priority;

    private LocalDate deadline;

    private OffsetDateTime plannedStartAt;

    private OffsetDateTime plannedEndAt;

    @Positive
    private Integer estimatedMinutes;

    @PositiveOrZero
    private BigDecimal estimatedCost;

    private UUID financeAccountId;

    private UUID monthlyIncomeGroupId;

    private UUID monthlyIncomeAccountId;

    @Pattern(regexp = "^[A-Z]{3}$")
    private String monthlyIncomeCurrency;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$")
    private String monthlyIncomePeriod;

    @Positive
    private BigDecimal monthlyIncomeBase;

    @PositiveOrZero
    private BigDecimal monthlyIncomeBonus;

    @PositiveOrZero
    private BigDecimal monthlyIncomeDeduction;

    private UUID categoryId;
}
