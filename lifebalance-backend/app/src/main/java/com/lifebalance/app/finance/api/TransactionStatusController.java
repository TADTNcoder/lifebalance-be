package com.lifebalance.app.finance.api;

import com.lifebalance.app.shared.api.ApiResponse;
import com.lifebalance.app.shared.api.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
class TransactionStatusController {

    @GetMapping("/status")
    ApiResponse<ModuleStatusResponse> status() {
        return ApiResponse.success(ModuleStatusResponse.ready("transactions"));
    }

}
