package com.lifebalance.task.integration;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskReminder;
import java.util.UUID;

public interface TaskIntegrationPublisher {

    void publishTaskChanged(Task task, UUID actorId, TaskIntegrationAction action, String reason);

    default void publishMonthlyIncomeReady(Task task, UUID actorId, String reason) {
        // Optional integration for task groups that settle a monthly income
        // only after every occurrence has been completed.
    }

    void publishReminderChanged(TaskReminder reminder, UUID actorId, TaskIntegrationAction action, String reason);
}
