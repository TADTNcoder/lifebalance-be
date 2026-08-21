package com.lifebalance.task.controller;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.ReminderRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.response.ReminderResponse;
import com.lifebalance.task.service.TaskReminderService;
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

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/reminders")
@RequiredArgsConstructor
public class TaskReminderController {

    private final TaskReminderService taskReminderService;

    @PostMapping
    public ReminderResponse create(
            @Valid @RequestBody ReminderRequest request,
            HttpServletRequest httpRequest) {

        return taskReminderService.create(
                getCurrentUserId(httpRequest),
                request);
    }

    @PutMapping("/{reminderId}")
    public ReminderResponse update(
            @PathVariable UUID reminderId,
            @Valid @RequestBody ReminderRequest request,
            HttpServletRequest httpRequest) {

        return taskReminderService.update(
                getCurrentUserId(httpRequest),
                reminderId,
                request);
    }

    @PatchMapping("/{reminderId}/cancel")
    public void cancel(
            @PathVariable UUID reminderId,
            @Valid @RequestBody(required = false) TaskLifecycleActionRequest request,
            HttpServletRequest httpRequest) {

        taskReminderService.cancel(
                getCurrentUserId(httpRequest),
                reminderId,
                request);
    }

    @GetMapping("/task/{taskId}")
    public Page<ReminderResponse> getByTask(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        Pageable pageable = PageableLimits.of(page, size);
        return taskReminderService.getByTask(
                getCurrentUserId(httpRequest),
                taskId,
                pageable);
    }

    @GetMapping("/upcoming")
    public Page<ReminderResponse> getUpcoming(
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        Pageable pageable = PageableLimits.of(page, size);
        return taskReminderService.getUpcoming(
                getCurrentUserId(httpRequest),
                from,
                to,
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
