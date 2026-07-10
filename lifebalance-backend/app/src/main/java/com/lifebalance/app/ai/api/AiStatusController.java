package com.lifebalance.app.ai.api;

import com.lifebalance.app.shared.api.ApiResponse;
import com.lifebalance.app.shared.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
class AiStatusController {

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(ModuleStatusResponse.ready("ai"));
    }

}
