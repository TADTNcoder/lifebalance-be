package com.lifebalance.task.service.impl;

import com.lifebalance.task.dto.request.RecurringRuleRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.response.RecurringRuleResponse;
import com.lifebalance.task.error.TaskExceptions;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskRecurringRule;
import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.repository.TaskRecurringRuleRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.service.TaskRecurringRuleService;
import com.lifebalance.task.validation.TaskLifecyclePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskRecurringRuleServiceImpl implements TaskRecurringRuleService {

    private final TaskRecurringRuleRepository taskRecurringRuleRepository;
    private final TaskRepository taskRepository;
    private final TaskLifecyclePolicy taskLifecyclePolicy;
    private final TaskChangeHistoryService taskChangeHistoryService;

    @Override
    @Transactional
    public RecurringRuleResponse create(
            UUID ownerId,
            RecurringRuleRequest request) {

        validateRequest(request);
        taskLifecyclePolicy.validateOptionalFeatureApproved(
                "recurring",
                request.getPolicyStatus(),
                request.getFeatureEnabled());

        Task task = findTask(request.getTaskId(), ownerId);
        TaskRecurringRule rule = TaskRecurringRule.builder()
                .ownerId(ownerId)
                .task(task)
                .createdBy(ownerId)
                .updatedBy(ownerId)
                .build();

        applyRequest(rule, request, ownerId);
        rule = taskRecurringRuleRepository.save(rule);
        taskChangeHistoryService.recordTaskChange(
                task,
                ownerId,
                TaskHistoryActionType.TASK_RECURRING_RULE_CREATED,
                "recurringRule",
                null,
                ruleSnapshot(rule),
                request.getReason());

        return mapToResponse(rule);
    }

    @Override
    @Transactional
    public RecurringRuleResponse update(
            UUID ownerId,
            UUID ruleId,
            RecurringRuleRequest request) {

        validateRequest(request);
        TaskRecurringRule rule = findRule(ruleId, ownerId);
        UUID currentTaskId = rule.getTask().getId();
        if (!Objects.equals(currentTaskId, request.getTaskId())) {
            throw TaskExceptions.recurringRuleInvalid("Task recurring rule task cannot be changed.");
        }
        taskLifecyclePolicy.validateOptionalFeatureApproved(
                "recurring",
                request.getPolicyStatus(),
                request.getFeatureEnabled());

        String oldSnapshot = ruleSnapshot(rule);
        applyRequest(rule, request, ownerId);
        rule = taskRecurringRuleRepository.save(rule);
        taskChangeHistoryService.recordTaskChange(
                rule.getTask(),
                ownerId,
                TaskHistoryActionType.TASK_RECURRING_RULE_UPDATED,
                "recurringRule",
                oldSnapshot,
                ruleSnapshot(rule),
                request.getReason());

        return mapToResponse(rule);
    }

    @Override
    @Transactional
    public void disable(
            UUID ownerId,
            UUID ruleId,
            TaskLifecycleActionRequest request) {

        TaskRecurringRule rule = findRule(ruleId, ownerId);
        String oldSnapshot = ruleSnapshot(rule);
        String reason = request == null
                ? null
                : request.getReason();

        rule.disable(ownerId, reason);
        rule = taskRecurringRuleRepository.save(rule);
        taskChangeHistoryService.recordTaskChange(
                rule.getTask(),
                ownerId,
                TaskHistoryActionType.TASK_RECURRING_RULE_DISABLED,
                "recurringRule",
                oldSnapshot,
                ruleSnapshot(rule),
                reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RecurringRuleResponse> getByTask(
            UUID ownerId,
            UUID taskId,
            Pageable pageable) {

        findTask(taskId, ownerId);
        return taskRecurringRuleRepository
                .findByOwnerIdAndTaskIdOrderByCreatedAtDesc(
                        ownerId,
                        taskId,
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

    private TaskRecurringRule findRule(
            UUID ruleId,
            UUID ownerId) {

        return taskRecurringRuleRepository
                .findByIdAndOwnerId(ruleId, ownerId)
                .orElseThrow(TaskExceptions::recurringRuleNotFound);
    }

    private void validateRequest(RecurringRuleRequest request) {
        if (request == null) {
            throw TaskExceptions.recurringRuleInvalid("Request body is required.");
        }
        if (request.getTaskId() == null) {
            throw TaskExceptions.recurringRuleInvalid("Task is required.");
        }
        if (request.getPolicyStatus() == null) {
            throw TaskExceptions.recurringRuleInvalid("Policy status is required.");
        }
        if (request.getFeatureEnabled() == null) {
            throw TaskExceptions.recurringRuleInvalid("Feature enabled flag is required.");
        }
        if (request.getRecurrenceType() == null) {
            throw TaskExceptions.recurringRuleInvalid("Recurrence type is required.");
        }
        if (request.getIntervalCount() != null && request.getIntervalCount() <= 0) {
            throw TaskExceptions.recurringRuleInvalid("Interval count must be greater than 0.");
        }
        if (request.getStartsOn() == null) {
            throw TaskExceptions.recurringRuleInvalid("Start date is required.");
        }
        if (request.getEndsOn() != null && request.getEndsOn().isBefore(request.getStartsOn())) {
            throw TaskExceptions.recurringRuleInvalid("End date must not be before start date.");
        }
        if (request.getMaxOccurrences() != null && request.getMaxOccurrences() <= 0) {
            throw TaskExceptions.recurringRuleInvalid("Maximum occurrences must be greater than 0.");
        }
        if (request.getPolicyStatus() == OptionalFeaturePolicyStatus.DISABLED
                && Boolean.TRUE.equals(request.getFeatureEnabled())) {
            throw TaskExceptions.recurringRuleInvalid("Disabled recurring policy cannot enable the feature.");
        }
    }

    private void applyRequest(
            TaskRecurringRule rule,
            RecurringRuleRequest request,
            UUID actorId) {

        rule.updateFrom(
                request.getPolicyStatus(),
                request.getFeatureEnabled(),
                request.getRecurrenceType(),
                request.getIntervalCount(),
                request.getDaysOfWeek(),
                request.getStartsOn(),
                request.getEndsOn(),
                request.getMaxOccurrences(),
                request.getTimezone(),
                request.getReason(),
                actorId);
    }

    private String ruleSnapshot(TaskRecurringRule rule) {
        return "ruleId=" + rule.getId()
                + ";taskId=" + rule.getTask().getId()
                + ";policyStatus=" + rule.getPolicyStatus()
                + ";featureEnabled=" + rule.getFeatureEnabled()
                + ";recurrenceType=" + rule.getRecurrenceType()
                + ";intervalCount=" + rule.getIntervalCount()
                + ";daysOfWeek=" + rule.getDaysOfWeek()
                + ";startsOn=" + rule.getStartsOn()
                + ";endsOn=" + rule.getEndsOn()
                + ";maxOccurrences=" + rule.getMaxOccurrences()
                + ";timezone=" + rule.getTimezone();
    }

    private RecurringRuleResponse mapToResponse(TaskRecurringRule rule) {
        RecurringRuleResponse response = new RecurringRuleResponse();
        response.setId(rule.getId());
        response.setOwnerId(rule.getOwnerId());
        response.setTaskId(rule.getTask().getId());
        response.setPolicyStatus(rule.getPolicyStatus());
        response.setFeatureEnabled(rule.getFeatureEnabled());
        response.setRecurrenceType(rule.getRecurrenceType());
        response.setIntervalCount(rule.getIntervalCount());
        response.setDaysOfWeek(rule.getDaysOfWeek());
        response.setStartsOn(rule.getStartsOn());
        response.setEndsOn(rule.getEndsOn());
        response.setMaxOccurrences(rule.getMaxOccurrences());
        response.setTimezone(rule.getTimezone());
        response.setReason(rule.getReason());
        response.setCreatedBy(rule.getCreatedBy());
        response.setUpdatedBy(rule.getUpdatedBy());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        return response;
    }
}
