package com.lifebalance.task.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public TaskResponse create(
            @RequestHeader("X-User-Id") UUID ownerId,
            @Valid @RequestBody CreateTaskRequest request) {

        return taskService.create(ownerId, request);
    }

    @GetMapping
    public Page<TaskResponse> search(
            @RequestHeader("X-User-Id") UUID ownerId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return taskService.search(
                ownerId,
                keyword,
                pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID id) {

        return taskService.getById(id, ownerId);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {

        return taskService.update(
                id,
                ownerId,
                request);
    }

    @PatchMapping("/{id}/archive")
    public void archive(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID id) {

        taskService.archive(id, ownerId);
    }

    @PatchMapping("/{id}/restore")
    public void restore(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID id) {

        taskService.restore(id, ownerId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID id) {

        taskService.delete(id, ownerId);
    }

    @PostMapping("/{id}/duplicate")
    public TaskResponse duplicate(
            @RequestHeader("X-User-Id") UUID ownerId,
            @PathVariable UUID id) {

        return taskService.duplicate(id, ownerId);
    }
}