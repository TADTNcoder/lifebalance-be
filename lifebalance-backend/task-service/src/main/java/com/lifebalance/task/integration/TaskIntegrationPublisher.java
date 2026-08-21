package com.lifebalance.task.integration;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskReminder;
import java.util.UUID;

public interface TaskIntegrationPublisher {

    void publishTaskChanged(Task task, UUID actorId, TaskIntegrationAction action, String reason);

    void publishReminderChanged(TaskReminder reminder, UUID actorId, TaskIntegrationAction action, String reason);
}
