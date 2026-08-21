package com.lifebalance.task.service.impl;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.dto.request.ReminderRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.error.TaskErrorCode;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.integration.TaskIntegrationPublisher;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskReminder;
import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.ReminderChannel;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.repository.TaskReminderRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.validation.TaskLifecyclePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskReminderServiceImplTest {

    @Mock
    private TaskReminderRepository taskReminderRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskChangeHistoryService taskChangeHistoryService;

    @Mock
    private TaskIntegrationPublisher taskIntegrationPublisher;

    private TaskReminderServiceImpl taskReminderService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private final UUID reminderId = UUID.randomUUID();

    private Task task;

    @BeforeEach
    void setUp() {
        taskReminderService = new TaskReminderServiceImpl(
                taskReminderRepository,
                taskRepository,
                new TaskLifecyclePolicy(),
                taskChangeHistoryService,
                taskIntegrationPublisher);
        task = Task.builder()
                .id(taskId)
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Prepare review")
                .build();
    }

    @Test
    void createApprovedReminderPersistsAndWritesHistory() {
        ReminderRequest request = request();
        request.setPolicyStatus(OptionalFeaturePolicyStatus.APPROVED);
        request.setFeatureEnabled(true);

        when(taskRepository.findByIdAndOwnerId(taskId, ownerId)).thenReturn(Optional.of(task));
        when(taskReminderRepository.save(any(TaskReminder.class))).thenAnswer(invocation -> {
            TaskReminder reminder = invocation.getArgument(0);
            reminder.setId(reminderId);
            return reminder;
        });

        var response = taskReminderService.create(ownerId, request);

        assertEquals(reminderId, response.getId());
        assertEquals(taskId, response.getTaskId());
        assertEquals(ReminderChannel.EMAIL, response.getChannel());
        verify(taskChangeHistoryService).recordTaskChange(
                eq(task),
                eq(ownerId),
                eq(TaskHistoryActionType.TASK_REMINDER_CREATED),
                eq("reminder"),
                isNull(),
                any(),
                eq("Reminder approved"));
    }

    @Test
    void createRejectsEnabledReminderWithoutApprovedPolicyBeforeLookupOrSave() {
        ReminderRequest request = request();
        request.setPolicyStatus(OptionalFeaturePolicyStatus.PENDING_APPROVAL);
        request.setFeatureEnabled(true);

        AppException exception = assertThrows(AppException.class, () -> taskReminderService.create(ownerId, request));

        assertEquals(TaskErrorCode.TASK_OPTIONAL_FEATURE_NOT_APPROVED, exception.getCode());
        verify(taskRepository, never()).findByIdAndOwnerId(any(), any());
        verify(taskReminderRepository, never()).save(any());
    }

    @Test
    void updateRejectsCancelledReminderBeforeSave() {
        ReminderRequest request = request();
        TaskReminder reminder = TaskReminder.builder()
                .id(reminderId)
                .ownerId(ownerId)
                .task(task)
                .policyStatus(OptionalFeaturePolicyStatus.APPROVED)
                .featureEnabled(true)
                .remindAt(OffsetDateTime.now().plusDays(2))
                .channel(ReminderChannel.IN_APP)
                .cancelledAt(OffsetDateTime.now())
                .build();

        when(taskReminderRepository.findByIdAndOwnerId(reminderId, ownerId)).thenReturn(Optional.of(reminder));

        AppException exception = assertThrows(AppException.class, () -> taskReminderService.update(ownerId, reminderId, request));

        assertEquals(TaskErrorCode.TASK_REMINDER_INVALID, exception.getCode());
        verify(taskReminderRepository, never()).save(any());
    }

    @Test
    void cancelMarksReminderDisabledAndWritesHistory() {
        TaskReminder reminder = TaskReminder.builder()
                .id(reminderId)
                .ownerId(ownerId)
                .task(task)
                .policyStatus(OptionalFeaturePolicyStatus.APPROVED)
                .featureEnabled(true)
                .remindAt(OffsetDateTime.now().plusDays(2))
                .channel(ReminderChannel.IN_APP)
                .build();
        TaskLifecycleActionRequest request = new TaskLifecycleActionRequest();
        request.setReason("No longer needed");

        when(taskReminderRepository.findByIdAndOwnerId(reminderId, ownerId)).thenReturn(Optional.of(reminder));
        when(taskReminderRepository.save(reminder)).thenReturn(reminder);

        taskReminderService.cancel(ownerId, reminderId, request);

        assertEquals(OptionalFeaturePolicyStatus.DISABLED, reminder.getPolicyStatus());
        assertEquals(false, reminder.getFeatureEnabled());
        verify(taskChangeHistoryService).recordTaskChange(
                eq(task),
                eq(ownerId),
                eq(TaskHistoryActionType.TASK_REMINDER_CANCELLED),
                eq("reminder"),
                any(),
                any(),
                eq("No longer needed"));
    }

    @Test
    void getUpcomingRejectsInvalidWindow() {
        OffsetDateTime now = OffsetDateTime.now();

        AppException exception = assertThrows(
                AppException.class,
                () -> taskReminderService.getUpcoming(ownerId, now, now, PageRequest.of(0, 10)));

        assertEquals(TaskErrorCode.TASK_REMINDER_INVALID, exception.getCode());
        verify(taskReminderRepository, never()).findByOwnerIdAndRemindAtBetweenOrderByRemindAtAsc(any(), any(), any(), any());
    }

    private ReminderRequest request() {
        ReminderRequest request = new ReminderRequest();
        request.setTaskId(taskId);
        request.setPolicyStatus(OptionalFeaturePolicyStatus.PENDING_APPROVAL);
        request.setFeatureEnabled(false);
        request.setRemindAt(OffsetDateTime.now().plusDays(1));
        request.setChannel(ReminderChannel.EMAIL);
        request.setMessage("Review task timeline");
        request.setReason("Reminder approved");
        return request;
    }
}
