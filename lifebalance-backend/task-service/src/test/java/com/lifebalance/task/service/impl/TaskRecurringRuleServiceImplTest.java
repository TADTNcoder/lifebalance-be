package com.lifebalance.task.service.impl;

import com.lifebalance.common.error.AppException;
import com.lifebalance.task.dto.request.RecurringRuleRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.error.TaskErrorCode;
import com.lifebalance.task.history.TaskChangeHistoryService;
import com.lifebalance.task.model.Task;
import com.lifebalance.task.model.TaskRecurringRule;
import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.RecurrenceType;
import com.lifebalance.task.model.enums.TaskHistoryActionType;
import com.lifebalance.task.repository.TaskRecurringRuleRepository;
import com.lifebalance.task.repository.TaskRepository;
import com.lifebalance.task.validation.TaskLifecyclePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
class TaskRecurringRuleServiceImplTest {

    @Mock
    private TaskRecurringRuleRepository taskRecurringRuleRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskChangeHistoryService taskChangeHistoryService;

    private TaskRecurringRuleServiceImpl taskRecurringRuleService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();
    private final UUID ruleId = UUID.randomUUID();

    private Task task;

    @BeforeEach
    void setUp() {
        taskRecurringRuleService = new TaskRecurringRuleServiceImpl(
                taskRecurringRuleRepository,
                taskRepository,
                new TaskLifecyclePolicy(),
                taskChangeHistoryService);
        task = Task.builder()
                .id(taskId)
                .ownerId(ownerId)
                .userId(ownerId)
                .name("Weekly planning")
                .build();
    }

    @Test
    void createApprovedRecurringRulePersistsAndWritesHistory() {
        RecurringRuleRequest request = request();
        request.setPolicyStatus(OptionalFeaturePolicyStatus.APPROVED);
        request.setFeatureEnabled(true);

        when(taskRepository.findByIdAndOwnerId(taskId, ownerId)).thenReturn(Optional.of(task));
        when(taskRecurringRuleRepository.save(any(TaskRecurringRule.class))).thenAnswer(invocation -> {
            TaskRecurringRule rule = invocation.getArgument(0);
            rule.setId(ruleId);
            return rule;
        });

        var response = taskRecurringRuleService.create(ownerId, request);

        assertEquals(ruleId, response.getId());
        assertEquals(taskId, response.getTaskId());
        assertEquals(OptionalFeaturePolicyStatus.APPROVED, response.getPolicyStatus());
        verify(taskChangeHistoryService).recordTaskChange(
                eq(task),
                eq(ownerId),
                eq(TaskHistoryActionType.TASK_RECURRING_RULE_CREATED),
                eq("recurringRule"),
                isNull(),
                any(),
                eq("Repeat focus block"));
    }

    @Test
    void createRejectsEnabledRecurringRuleWithoutApprovedPolicyBeforeLookupOrSave() {
        RecurringRuleRequest request = request();
        request.setPolicyStatus(OptionalFeaturePolicyStatus.PENDING_APPROVAL);
        request.setFeatureEnabled(true);

        AppException exception = assertThrows(AppException.class, () -> taskRecurringRuleService.create(ownerId, request));

        assertEquals(TaskErrorCode.TASK_OPTIONAL_FEATURE_NOT_APPROVED, exception.getCode());
        verify(taskRepository, never()).findByIdAndOwnerId(any(), any());
        verify(taskRecurringRuleRepository, never()).save(any());
    }

    @Test
    void updateRejectsChangingRuleTask() {
        RecurringRuleRequest request = request();
        request.setTaskId(UUID.randomUUID());
        TaskRecurringRule rule = TaskRecurringRule.builder()
                .id(ruleId)
                .ownerId(ownerId)
                .task(task)
                .policyStatus(OptionalFeaturePolicyStatus.PENDING_APPROVAL)
                .featureEnabled(false)
                .recurrenceType(RecurrenceType.WEEKLY)
                .startsOn(LocalDate.now())
                .build();

        when(taskRecurringRuleRepository.findByIdAndOwnerId(ruleId, ownerId)).thenReturn(Optional.of(rule));

        AppException exception = assertThrows(AppException.class, () -> taskRecurringRuleService.update(ownerId, ruleId, request));

        assertEquals(TaskErrorCode.TASK_RECURRING_RULE_INVALID, exception.getCode());
        verify(taskRecurringRuleRepository, never()).save(any());
    }

    @Test
    void disableMarksFeatureDisabledAndWritesHistory() {
        TaskRecurringRule rule = TaskRecurringRule.builder()
                .id(ruleId)
                .ownerId(ownerId)
                .task(task)
                .policyStatus(OptionalFeaturePolicyStatus.APPROVED)
                .featureEnabled(true)
                .recurrenceType(RecurrenceType.WEEKLY)
                .startsOn(LocalDate.now())
                .build();
        TaskLifecycleActionRequest request = new TaskLifecycleActionRequest();
        request.setReason("Policy withdrawn");

        when(taskRecurringRuleRepository.findByIdAndOwnerId(ruleId, ownerId)).thenReturn(Optional.of(rule));
        when(taskRecurringRuleRepository.save(rule)).thenReturn(rule);

        taskRecurringRuleService.disable(ownerId, ruleId, request);

        assertEquals(OptionalFeaturePolicyStatus.DISABLED, rule.getPolicyStatus());
        assertEquals(false, rule.getFeatureEnabled());
        verify(taskChangeHistoryService).recordTaskChange(
                eq(task),
                eq(ownerId),
                eq(TaskHistoryActionType.TASK_RECURRING_RULE_DISABLED),
                eq("recurringRule"),
                any(),
                any(),
                eq("Policy withdrawn"));
    }

    private RecurringRuleRequest request() {
        RecurringRuleRequest request = new RecurringRuleRequest();
        request.setTaskId(taskId);
        request.setPolicyStatus(OptionalFeaturePolicyStatus.PENDING_APPROVAL);
        request.setFeatureEnabled(false);
        request.setRecurrenceType(RecurrenceType.WEEKLY);
        request.setIntervalCount(1);
        request.setDaysOfWeek("MON,WED,FRI");
        request.setStartsOn(LocalDate.now().plusDays(1));
        request.setTimezone("Asia/Bangkok");
        request.setReason("Repeat focus block");
        return request;
    }
}
