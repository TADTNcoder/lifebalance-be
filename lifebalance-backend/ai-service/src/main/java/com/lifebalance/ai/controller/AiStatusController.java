package com.lifebalance.ai.controller;

import com.lifebalance.ai.service.AiStatusService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
class AiStatusController {

    private final AiStatusService statusService;

    AiStatusController(AiStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("ai"));
    }

}
