package com.lifebalance.task.controller;

import com.lifebalance.task.service.TaskStatusService;
import com.lifebalance.task.shared.api.ApiResponse;
import com.lifebalance.task.shared.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
class TaskStatusController {

    private final TaskStatusService statusService;

    TaskStatusController(TaskStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("tasks"));
    }

}
