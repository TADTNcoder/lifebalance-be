package com.lifebalance.task.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.validation.ValidPlanningWindow;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
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

    @Size(max = 3)
    private String currency;

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

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean financeAccountIdProvided;

    private UUID categoryId;

    @Size(max = 500)
    private String reason;

    public void setFinanceAccountId(UUID financeAccountId) {
        this.financeAccountId = financeAccountId;
        this.financeAccountIdProvided = true;
    }

    @JsonIgnore
    public boolean hasFinanceAccountId() {
        return financeAccountIdProvided;
    }
}
