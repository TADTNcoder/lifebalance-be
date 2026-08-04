package com.lifebalance.task.controller;

import org.springframework.web.bind.annotation.*;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.service.TaskService;
import java.util.UUID;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // @Operation(summary = "Create Task", description = "Create a new task")
    // @ApiResponses({
    // @ApiResponse(responseCode = "200", description = "Task created
    // successfully"),
    // @ApiResponse(responseCode = "400", description = "Validation failed")
    // })
    @PostMapping
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request);
    }

    @PostMapping("/{id}/duplicate")
    public TaskResponse duplicate(@PathVariable UUID id) {
        return taskService.duplicate(id);
    }

    @GetMapping
    public Page<TaskResponse> search(
            @RequestParam(defaultValue = "") String keyword,
            Pageable pageable) {

        return taskService.search(keyword, pageable);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {

        return taskService.update(id, request);
    }

    @PatchMapping("/{id}/archive")
    public void archive(@PathVariable UUID id) {
        taskService.archive(id);
    }

    @PatchMapping("/{id}/restore")
    public void restore(@PathVariable UUID id) {
        taskService.restore(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        taskService.delete(id);
    }

}
