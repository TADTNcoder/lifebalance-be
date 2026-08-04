package com.lifebalance.task.controller;

import org.springframework.web.bind.annotation.*;

import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.service.TaskService;
import java.util.UUID;
import com.lifebalance.task.dto.request.UpdateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {

        return taskService.update(id, request);
    }
}
