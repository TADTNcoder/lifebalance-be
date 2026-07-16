package com.lifebalance.finance.controller;

import com.lifebalance.finance.service.FinanceStatusService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
class FinanceStatusController {

    private final FinanceStatusService statusService;

    FinanceStatusController(FinanceStatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("finance"));
    }

}
