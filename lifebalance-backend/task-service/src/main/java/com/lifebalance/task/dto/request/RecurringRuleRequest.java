package com.lifebalance.task.dto.request;

import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.RecurrenceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class RecurringRuleRequest {

    @NotNull
    private UUID taskId;

    @NotNull
    private OptionalFeaturePolicyStatus policyStatus;

    @NotNull
    private Boolean featureEnabled;

    @NotNull
    private RecurrenceType recurrenceType;

    @Positive
    private Integer intervalCount;

    @Size(max = 64)
    private String daysOfWeek;

    @NotNull
    private LocalDate startsOn;

    private LocalDate endsOn;

    @Positive
    private Integer maxOccurrences;

    @Size(max = 64)
    private String timezone;

    @Size(max = 1000)
    private String reason;
}
