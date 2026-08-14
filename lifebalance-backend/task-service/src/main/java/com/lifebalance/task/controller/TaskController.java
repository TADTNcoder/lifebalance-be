package com.lifebalance.task.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        Pageable pageable = PageRequest.of(page, size);

        return taskService.search(
                ownerId,
                keyword,
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

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {

        UUID ownerId = getCurrentUserId(httpRequest);

        taskService.delete(
                id,
                ownerId);
    }

    private UUID getCurrentUserId(
            HttpServletRequest request) {

        KeycloakUserPrincipal currentUser = (KeycloakUserPrincipal) request.getAttribute(
                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE);

        if (currentUser == null
                || currentUser.userId() == null) {

            throw new RuntimeException(
                    "Authenticated user not found");
        }

        return currentUser.userId();
    }
}