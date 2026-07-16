package com.lifebalance.notification.controller;

import com.lifebalance.notification.service.NotificationStatusService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
class NotificationStatusController {

    private final NotificationStatusService statusService;

    NotificationStatusController(NotificationStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("notifications"));
    }

}
