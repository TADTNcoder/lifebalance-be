package com.lifebalance.task.controller;

import org.springframework.web.bind.annotation.*;

import com.lifebalance.task.dto.request.CreateTaskRequest;
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
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request);
    }
}
