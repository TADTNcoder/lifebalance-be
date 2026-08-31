package com.lifebalance.task.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.lifebalance.common.web.PageableLimits;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.TaskLifecycleActionRequest;
import com.lifebalance.task.dto.request.TaskPlanningRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskProgressRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.service.TaskService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public TaskResponse create(
            @Valid @RequestBody CreateTaskRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.create(
                ownerId,
                request);
    }

    @PostMapping("/{id}/duplicate")
    public TaskResponse duplicate(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.duplicate(
                id,
                ownerId);
    }

    @GetMapping
    public Page<TaskResponse> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) PriorityLevel priority,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate deadlineFrom,
            @RequestParam(required = false) LocalDate deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        Pageable pageable = PageableLimits.of(
                page,
                size,
                TaskSortCriteria.toSort(sortBy, sortDirection));

        return taskService.search(
                ownerId,
                keyword,
                status,
                priority,
                categoryId,
                deadlineFrom,
                deadlineTo,
                pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.getById(
                id,
                ownerId);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.update(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/plan")
    public TaskResponse plan(
            @PathVariable UUID id,
            @Valid @RequestBody TaskPlanningRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.plan(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/progress")
    public TaskResponse updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskProgressRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.updateProgress(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/archive")
    public void archive(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        taskService.archive(
                id,
                ownerId);
    }

    @PatchMapping("/{id}/restore")
    public void restore(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        taskService.restore(
                id,
                ownerId);
    }

    @PatchMapping("/{id}/pause")
    public TaskResponse pause(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) TaskLifecycleActionRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.pause(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/resume")
    public TaskResponse resume(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) TaskLifecycleActionRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.resume(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) TaskLifecycleActionRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.complete(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/cancel")
    public TaskResponse cancel(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) TaskLifecycleActionRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.cancel(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/reopen")
    public TaskResponse reopen(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) TaskLifecycleActionRequest request,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        return taskService.reopen(
                id,
                ownerId,
                request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        taskService.delete(
                id,
                ownerId);
    }

    @DeleteMapping("/{id}/finance-link")
    public void deleteFinanceLinkedTask(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        taskService.deleteFinanceLinkedTask(
                id,
                ownerId);
    }

    private UUID getCurrentUserId(
            HttpServletRequest request) {

        KeycloakUserPrincipal currentUser = (KeycloakUserPrincipal) request.getAttribute(
                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE);

        if (currentUser == null
                || currentUser.userId() == null) {

            throw new AuthenticationCredentialsNotFoundException(
                    "Authenticated internal user id is required.");
        }

        return currentUser.userId();
    }
}
