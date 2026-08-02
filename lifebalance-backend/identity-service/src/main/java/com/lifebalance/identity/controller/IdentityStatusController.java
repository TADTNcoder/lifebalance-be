package com.lifebalance.identity.controller;

import com.lifebalance.identity.service.IdentityStatusService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/identity")
@Tag(name = "Health")
@SecurityRequirements
class IdentityStatusController {

    private final IdentityStatusService statusService;

    IdentityStatusController(IdentityStatusService statusService) {
        this.statusService = statusService;
    }

    @Operation(summary = "Get identity module status", description = "Lightweight readiness endpoint for service discovery and smoke checks")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Module is ready",
            content = @Content(schema = @Schema(implementation = com.lifebalance.common.api.ApiResponse.class))
    )
    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(statusService.status("identity"));
    }

}
