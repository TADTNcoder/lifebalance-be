package com.lifebalance.task.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskReminder;
import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.ReminderChannel;
import com.lifebalance.task.model.enums.TaskStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
class AfterCommitTaskIntegrationPublisherTest {

    @Mock
    private TaskIntegrationClient client;

    private TaskIntegrationProperties properties;
    private AfterCommitTaskIntegrationPublisher publisher;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        properties = new TaskIntegrationProperties();
        publisher = new AfterCommitTaskIntegrationPublisher(properties, client);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void publishesTimelineSyncOnlyAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        Task task = task(TaskStatus.PLANNED);

        publisher.publishTaskChanged(task, ownerId, TaskIntegrationAction.TASK_PLANNED, "Plan");

        verifyNoInteractions(client);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCommit());

        verify(client).syncTimelineTask(argThat(event ->
                event.taskId().equals(taskId)
                        && event.action() == TaskIntegrationAction.TASK_PLANNED
                        && event.taskStatus() == TaskStatus.PLANNED));
        verify(client, never()).createNotification(any(), any());
        verify(client, never()).recordActualSeed(any(), any());
    }

    @Test
    void completedTaskCreatesNotificationAndOptionalAnalyticsSeedWhenPoliciesAllow() {
        properties.getNotificationService().setPolicyApproved(true);
        properties.getAnalyticsService().setActualSeedEnabled(true);
        properties.getAnalyticsService().setDefaultCurrencyCode("usd");
        Task task = task(TaskStatus.COMPLETED);
        task.setEstimatedMinutes(90);
        task.setEstimatedCost(new BigDecimal("25.50"));
        task.setCompletedAt(OffsetDateTime.parse("2026-08-21T10:00:00+07:00"));

        publisher.publishTaskChanged(task, ownerId, TaskIntegrationAction.TASK_COMPLETED, "Done");

        verify(client).syncTimelineTask(any(TaskIntegrationEvent.class));
        verify(client).createNotification(argThat(request ->
                "ACTUAL_RECORDING_REMINDER".equals(request.eventType())
                        && taskId.equals(request.referenceId())
                        && Boolean.TRUE.equals(request.policyApproved())), isNull());
        verify(client).recordActualSeed(argThat(request ->
                "TIME_AND_MONEY".equals(request.recordType())
                        && taskId.equals(request.taskId())
                        && Integer.valueOf(90).equals(request.actualMinutes())
                        && new BigDecimal("25.50").compareTo(request.actualCost()) == 0
                        && "USD".equals(request.currencyCode())), isNull());
    }

    @Test
    void reminderChangeCreatesScheduledReminderNotificationWhenPolicyAllows() {
        properties.getNotificationService().setPolicyApproved(true);
        TaskReminder reminder = TaskReminder.builder()
                .id(UUID.randomUUID())
                .ownerId(ownerId)
                .task(task(TaskStatus.PLANNED))
                .policyStatus(OptionalFeaturePolicyStatus.APPROVED)
                .featureEnabled(true)
                .channel(ReminderChannel.EMAIL)
                .remindAt(OffsetDateTime.parse("2026-08-22T08:00:00+07:00"))
                .message("Prepare the review")
                .build();

        publisher.publishReminderChanged(reminder, ownerId, TaskIntegrationAction.TASK_REMINDER_CREATED, "Approved");

        verify(client).createNotification(argThat(request ->
                "TASK_REMINDER".equals(request.eventType())
                        && reminder.getId().equals(request.referenceId())
                        && request.channels().contains("EMAIL")
                        && reminder.getRemindAt().equals(request.scheduledAt())), isNull());
    }

    private Task task(TaskStatus status) {
        return Task.builder()
                .id(taskId)
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Write sprint review")
                .status(status)
                .estimatedMinutes(45)
                .build();
    }
}
