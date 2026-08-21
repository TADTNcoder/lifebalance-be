package com.lifebalance.task.dto.response;

import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.RecurrenceType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class RecurringRuleResponse {

    private UUID id;

    private UUID ownerId;

    private UUID taskId;

    private OptionalFeaturePolicyStatus policyStatus;

    private Boolean featureEnabled;

    private RecurrenceType recurrenceType;

    private Integer intervalCount;

    private String daysOfWeek;

    private LocalDate startsOn;

    private LocalDate endsOn;

    private Integer maxOccurrences;

    private String timezone;

    private String reason;

    private UUID createdBy;

    private UUID updatedBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
