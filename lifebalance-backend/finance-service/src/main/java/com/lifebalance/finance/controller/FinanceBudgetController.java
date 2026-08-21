package com.lifebalance.finance.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.finance.domain.BudgetStatus;
import com.lifebalance.finance.dto.BudgetResponse;
import com.lifebalance.finance.dto.BudgetStatusResponse;
import com.lifebalance.finance.dto.CreateBudgetRequest;
import com.lifebalance.finance.dto.PageResponse;
import com.lifebalance.finance.dto.ReasonRequest;
import com.lifebalance.finance.dto.UpdateBudgetRequest;
import com.lifebalance.finance.service.FinanceBudgetService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class FinanceBudgetController {

    private final FinanceBudgetService financeBudgetService;

    public FinanceBudgetController(FinanceBudgetService financeBudgetService) {
        this.financeBudgetService = financeBudgetService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> create(
            @Valid @RequestBody CreateBudgetRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        BudgetResponse response = financeBudgetService.create(CurrentFinanceUser.ownerId(currentUser), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeBudgetService.update(CurrentFinanceUser.ownerId(currentUser), id, request)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<BudgetResponse>> archive(
            @PathVariable UUID id,
            @Valid @RequestBody ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeBudgetService.archive(CurrentFinanceUser.ownerId(currentUser), id, request.reason())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeBudgetService.getById(CurrentFinanceUser.ownerId(currentUser), id)));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BudgetStatusResponse>> getBudgetStatus(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeBudgetService.getBudgetStatus(CurrentFinanceUser.ownerId(currentUser), id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BudgetResponse>>> getBudgets(
            @RequestParam(required = false) BudgetStatus status,
            @RequestParam(required = false) String currencyCode,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(financeBudgetService.getBudgets(
                CurrentFinanceUser.ownerId(currentUser),
                status,
                currencyCode,
                categoryId,
                fromDate,
                toDate,
                PageableLimits.normalize(pageable)
        ))));
    }
}
