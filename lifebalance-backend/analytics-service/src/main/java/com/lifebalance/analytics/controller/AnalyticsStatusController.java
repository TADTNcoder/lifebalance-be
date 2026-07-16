package com.lifebalance.analytics.controller;

import com.lifebalance.analytics.service.AnalyticsStatusService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
class AnalyticsStatusController {

    private final AnalyticsStatusService statusService;

    AnalyticsStatusController(AnalyticsStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("analytics"));
    }

}
