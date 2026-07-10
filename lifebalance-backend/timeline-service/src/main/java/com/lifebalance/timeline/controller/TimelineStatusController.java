package com.lifebalance.timeline.controller;

import com.lifebalance.timeline.service.TimelineStatusService;
import com.lifebalance.timeline.shared.api.ApiResponse;
import com.lifebalance.timeline.shared.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline")
class TimelineStatusController {

    private final TimelineStatusService statusService;

    TimelineStatusController(TimelineStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("timeline"));
    }

}
