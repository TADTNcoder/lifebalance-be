package com.lifebalance.task.service.impl;

import com.lifebalance.task.dto.request.ReminderRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.response.ReminderResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskReminder;
import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.ReminderChannel;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.repository.TaskReminderRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.service.TaskReminderService;
import com.lifebalance.task.validation.TaskLifecyclePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskReminderServiceImpl implements TaskReminderService {

    private final TaskReminderRepository taskReminderRepository;
    private final TaskRepository taskRepository;
    private final TaskLifecyclePolicy taskLifecyclePolicy;
    private final TaskChangeHistoryService taskChangeHistoryService;

    @Override
    @Transactional
    public ReminderResponse create(
            UUID ownerId,
            ReminderRequest request) {

        validateRequest(request);
        taskLifecyclePolicy.validateOptionalFeatureApproved(
                "reminder",
                request.getPolicyStatus(),
                request.getFeatureEnabled());

        Task task = findTask(request.getTaskId(), ownerId);
        TaskReminder reminder = TaskReminder.builder()
                .ownerId(ownerId)
                .task(task)
                .createdBy(ownerId)
                .updatedBy(ownerId)
                .build();

        applyRequest(reminder, request, ownerId);
        reminder = taskReminderRepository.save(reminder);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_REMINDER_CREATED,
                "reminder",
                null,
                reminderSnapshot(reminder),
                request.getReason());

        return mapToResponse(reminder);
    }

    @Override
    @Transactional
    public ReminderResponse update(
            UUID ownerId,
            UUID reminderId,
            ReminderRequest request) {

        validateRequest(request);
        TaskReminder reminder = findReminder(reminderId, ownerId);
        UUID currentTaskId = reminder.getTask().getId();
        if (!Objects.equals(currentTaskId, request.getTaskId())) {
            throw TaskExceptions.reminderInvalid("Task reminder task cannot be changed.");
        }
        if (reminder.getCancelledAt() != null) {
            throw TaskExceptions.reminderInvalid("Cancelled reminder cannot be updated.");
        }
        taskLifecyclePolicy.validateOptionalFeatureApproved(
                "reminder",
                request.getPolicyStatus(),
                request.getFeatureEnabled());

        String oldSnapshot = reminderSnapshot(reminder);
        applyRequest(reminder, request, ownerId);
        reminder = taskReminderRepository.save(reminder);
        taskChangeHistoryService.recordTaskChange(
                reminder.getTask(),
                ownerId,
                TaskHistoryActionType.TASK_REMINDER_UPDATED,
                "reminder",
                oldSnapshot,
                reminderSnapshot(reminder),
                request.getReason());

        return mapToResponse(reminder);
    }

    @Override
    @Transactional
    public void cancel(
            UUID ownerId,
            UUID reminderId,
            TaskLifecycleActionRequest request) {

        TaskReminder reminder = findReminder(reminderId, ownerId);
        String oldSnapshot = reminderSnapshot(reminder);
        String reason = request == null
                ? null
                : request.getReason();

        reminder.cancel(ownerId, reason);
        reminder = taskReminderRepository.save(reminder);
        taskChangeHistoryService.recordTaskChange(
                reminder.getTask(),
                ownerId,
                TaskHistoryActionType.TASK_REMINDER_CANCELLED,
                "reminder",
                oldSnapshot,
                reminderSnapshot(reminder),
                reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReminderResponse> getByTask(
            UUID ownerId,
            UUID taskId,
            Pageable pageable) {

        findTask(taskId, ownerId);
        return taskReminderRepository
                .findByOwnerIdAndTaskIdOrderByRemindAtAsc(
                        ownerId,
                        taskId,
                        pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReminderResponse> getUpcoming(
            UUID ownerId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable) {

        if (from == null || to == null || !from.isBefore(to)) {
            throw TaskExceptions.reminderInvalid("Reminder search window must have start before end.");
        }

        return taskReminderRepository
                .findByOwnerIdAndRemindAtBetweenOrderByRemindAtAsc(
                        ownerId,
                        from,
                        to,
                        pageable)
                .map(this::mapToResponse);
    }

    private Task findTask(
            UUID taskId,
            UUID ownerId) {

        return taskRepository
                .findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(TaskExceptions::taskNotFound);
    }

    private TaskReminder findReminder(
            UUID reminderId,
            UUID ownerId) {

        return taskReminderRepository
                .findByIdAndOwnerId(reminderId, ownerId)
                .orElseThrow(TaskExceptions::reminderNotFound);
    }

    private void validateRequest(ReminderRequest request) {
        if (request == null) {
            throw TaskExceptions.reminderInvalid("Request body is required.");
        }
        if (request.getTaskId() == null) {
            throw TaskExceptions.reminderInvalid("Task is required.");
        }
        if (request.getPolicyStatus() == null) {
            throw TaskExceptions.reminderInvalid("Policy status is required.");
        }
        if (request.getFeatureEnabled() == null) {
            throw TaskExceptions.reminderInvalid("Feature enabled flag is required.");
        }
        if (request.getPolicyStatus() == OptionalFeaturePolicyStatus.DISABLED
                && Boolean.TRUE.equals(request.getFeatureEnabled())) {
            throw TaskExceptions.reminderInvalid("Disabled reminder policy cannot enable the feature.");
        }
        if (request.getRemindAt() == null) {
            throw TaskExceptions.reminderInvalid("Reminder time is required.");
        }
        if (!request.getRemindAt().isAfter(OffsetDateTime.now())) {
            throw TaskExceptions.reminderInvalid("Reminder time must be in the future.");
        }
    }

    private void applyRequest(
            TaskReminder reminder,
            ReminderRequest request,
            UUID actorId) {

        reminder.updateFrom(
                request.getPolicyStatus(),
                request.getFeatureEnabled(),
                request.getRemindAt(),
                request.getChannel(),
                request.getMessage(),
                request.getReason(),
                actorId);
    }

    private String reminderSnapshot(TaskReminder reminder) {
        ReminderChannel channel = reminder.getChannel() == null
                ? ReminderChannel.IN_APP
                : reminder.getChannel();
        return "reminderId=" + reminder.getId()
                + ";taskId=" + reminder.getTask().getId()
                + ";policyStatus=" + reminder.getPolicyStatus()
                + ";featureEnabled=" + reminder.getFeatureEnabled()
                + ";remindAt=" + reminder.getRemindAt()
                + ";channel=" + channel
                + ";sentAt=" + reminder.getSentAt()
                + ";cancelledAt=" + reminder.getCancelledAt();
    }

    private ReminderResponse mapToResponse(TaskReminder reminder) {
        ReminderResponse response = new ReminderResponse();
        response.setId(reminder.getId());
        response.setOwnerId(reminder.getOwnerId());
        response.setTaskId(reminder.getTask().getId());
        response.setPolicyStatus(reminder.getPolicyStatus());
        response.setFeatureEnabled(reminder.getFeatureEnabled());
        response.setRemindAt(reminder.getRemindAt());
        response.setChannel(reminder.getChannel());
        response.setMessage(reminder.getMessage());
        response.setSentAt(reminder.getSentAt());
        response.setCancelledAt(reminder.getCancelledAt());
        response.setReason(reminder.getReason());
        response.setCreatedBy(reminder.getCreatedBy());
        response.setUpdatedBy(reminder.getUpdatedBy());
        response.setCreatedAt(reminder.getCreatedAt());
        response.setUpdatedAt(reminder.getUpdatedAt());
        return response;
    }
}
