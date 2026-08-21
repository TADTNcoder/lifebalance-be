package com.lifebalance.task.integration;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

record TaskNotificationRequest(
        String eventType,
        Set<String> channels,
        String priority,
        String title,
        String message,
        String referenceType,
        UUID referenceId,
        String purpose,
        Boolean policyApproved,
        OffsetDateTime scheduledAt,
        String reason
) {
}
