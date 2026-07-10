package com.lifebalance.app.task.api;

import com.lifebalance.app.shared.api.ApiResponse;
import com.lifebalance.app.shared.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
class TaskStatusController {

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(ModuleStatusResponse.ready("tasks"));
    }

}
