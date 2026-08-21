package com.lifebalance.task.controller;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.RecurringRuleRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.response.RecurringRuleResponse;
import com.lifebalance.task.service.TaskRecurringRuleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/recurring-rules")
@RequiredArgsConstructor
public class TaskRecurringRuleController {

    private final TaskRecurringRuleService taskRecurringRuleService;

    @PostMapping
    public RecurringRuleResponse create(
            @Valid @RequestBody RecurringRuleRequest request,
            HttpServletRequest httpRequest) {

        return taskRecurringRuleService.create(
                getCurrentUserId(httpRequest),
                request);
    }

    @PutMapping("/{ruleId}")
    public RecurringRuleResponse update(
            @PathVariable UUID ruleId,
            @Valid @RequestBody RecurringRuleRequest request,
            HttpServletRequest httpRequest) {

        return taskRecurringRuleService.update(
                getCurrentUserId(httpRequest),
                ruleId,
                request);
    }

    @PatchMapping("/{ruleId}/disable")
    public void disable(
            @PathVariable UUID ruleId,
            @Valid @RequestBody(required = false) TaskLifecycleActionRequest request,
            HttpServletRequest httpRequest) {

        taskRecurringRuleService.disable(
                getCurrentUserId(httpRequest),
                ruleId,
                request);
    }

    @GetMapping("/task/{taskId}")
    public Page<RecurringRuleResponse> getByTask(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        Pageable pageable = PageableLimits.of(page, size);
        return taskRecurringRuleService.getByTask(
                getCurrentUserId(httpRequest),
                taskId,
                pageable);
    }

    private UUID getCurrentUserId(HttpServletRequest request) {
        KeycloakUserPrincipal currentUser = (KeycloakUserPrincipal) request.getAttribute(
                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE);

        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated internal user id is required.");
        }

        return currentUser.userId();
    }
}
