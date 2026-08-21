package com.lifebalance.task.dto.request;

import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.ReminderChannel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReminderRequest {

    @NotNull
    private UUID taskId;

    @NotNull
    private OptionalFeaturePolicyStatus policyStatus;

    @NotNull
    private Boolean featureEnabled;

    @NotNull
    private OffsetDateTime remindAt;

    private ReminderChannel channel;

    @Size(max = 1000)
    private String message;

    @Size(max = 1000)
    private String reason;
}
