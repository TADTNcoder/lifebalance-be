package com.lifebalance.identity.controller;

import com.lifebalance.identity.service.IdentityStatusService;
import com.lifebalance.identity.shared.api.ApiResponse;
import com.lifebalance.identity.shared.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
class IdentityStatusController {

    private final IdentityStatusService statusService;

    IdentityStatusController(IdentityStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("identity"));
    }

}
