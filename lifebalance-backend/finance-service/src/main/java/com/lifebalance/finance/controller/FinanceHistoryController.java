package com.lifebalance.finance.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.finance.domain.FinanceReferenceType;
import com.lifebalance.finance.dto.FinanceHistoryResponse;
import com.lifebalance.finance.dto.PageResponse;
import com.lifebalance.finance.service.FinanceHistoryService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/history")
public class FinanceHistoryController {

    private final FinanceHistoryService financeHistoryService;

    public FinanceHistoryController(FinanceHistoryService financeHistoryService) {
        this.financeHistoryService = financeHistoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FinanceHistoryResponse>>> getHistory(
            @RequestParam(required = false) FinanceReferenceType referenceType,
            @RequestParam(required = false) UUID referenceId,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(financeHistoryService.getHistory(
                CurrentFinanceUser.ownerId(currentUser),
                referenceType,
                referenceId,
                PageableLimits.normalize(pageable)
        ))));
    }
}
