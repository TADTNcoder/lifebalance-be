package com.lifebalance.finance.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.finance.dto.FinanceSummaryResponse;
import com.lifebalance.finance.service.FinanceReportService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import java.time.OffsetDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/reports")
public class FinanceReportController {

    private final FinanceReportService financeReportService;

    public FinanceReportController(FinanceReportService financeReportService) {
        this.financeReportService = financeReportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<FinanceSummaryResponse>> getSummary(
            @RequestParam String currencyCode,
            @RequestParam(required = false) OffsetDateTime fromDate,
            @RequestParam(required = false) OffsetDateTime toDate,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(financeReportService.getSummary(
                CurrentFinanceUser.ownerId(currentUser),
                currencyCode,
                fromDate,
                toDate
        )));
    }
}
