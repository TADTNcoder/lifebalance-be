package com.lifebalance.task.integration;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskReminder;
import com.lifebalance.task.model.enums.ReminderChannel;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskReminderIntegrationEvent(
        UUID ownerId,
        UUID actorId,
        UUID taskId,
        UUID reminderId,
        String taskTitle,
        ReminderChannel channel,
        OffsetDateTime remindAt,
        String message,
        TaskIntegrationAction action,
        String reason,
        String authorizationHeader
) {

    static TaskReminderIntegrationEvent from(
            TaskReminder reminder,
            UUID actorId,
            TaskIntegrationAction action,
            String reason,
            String authorizationHeader
    ) {
        Task task = reminder.getTask();
        return new TaskReminderIntegrationEvent(
                reminder.getOwnerId(),
                actorId,
                task.getId(),
                reminder.getId(),
                task.getName(),
                reminder.getChannel(),
                reminder.getRemindAt(),
                reminder.getMessage(),
                action,
                reason,
                authorizationHeader
        );
    }
}
