package com.lifebalance.task.dto.response;

import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.ReminderChannel;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReminderResponse {

    private UUID id;

    private UUID ownerId;

    private UUID taskId;

    private OptionalFeaturePolicyStatus policyStatus;

    private Boolean featureEnabled;

    private OffsetDateTime remindAt;

    private ReminderChannel channel;

    private String message;

    private OffsetDateTime sentAt;

    private OffsetDateTime cancelledAt;

    private String reason;

    private UUID createdBy;

    private UUID updatedBy;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
