package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.RecurringRuleRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.response.RecurringRuleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskRecurringRuleService {

    RecurringRuleResponse create(
            UUID ownerId,
            RecurringRuleRequest request);

    RecurringRuleResponse update(
            UUID ownerId,
            UUID ruleId,
            RecurringRuleRequest request);

    void disable(
            UUID ownerId,
            UUID ruleId,
            TaskLifecycleActionRequest request);

    Page<RecurringRuleResponse> getByTask(
            UUID ownerId,
            UUID taskId,
            Pageable pageable);
}
