package com.lifebalance.finance.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.finance.domain.FinanceCategoryStatus;
import com.lifebalance.finance.domain.FinanceCategoryType;
import com.lifebalance.finance.dto.CreateFinanceCategoryRequest;
import com.lifebalance.finance.dto.FinanceCategoryResponse;
import com.lifebalance.finance.dto.PageResponse;
import com.lifebalance.finance.dto.ReasonRequest;
import com.lifebalance.finance.dto.UpdateFinanceCategoryRequest;
import com.lifebalance.finance.service.FinanceCategoryService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
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
@RequestMapping("/api/finance/categories")
public class FinanceCategoryController {

    private final FinanceCategoryService financeCategoryService;

    public FinanceCategoryController(FinanceCategoryService financeCategoryService) {
        this.financeCategoryService = financeCategoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FinanceCategoryResponse>> create(
            @Valid @RequestBody CreateFinanceCategoryRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        FinanceCategoryResponse response = financeCategoryService.create(CurrentFinanceUser.ownerId(currentUser), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FinanceCategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFinanceCategoryRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeCategoryService.update(CurrentFinanceUser.ownerId(currentUser), id, request)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<FinanceCategoryResponse>> archive(
            @PathVariable UUID id,
            @Valid @RequestBody ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeCategoryService.archive(CurrentFinanceUser.ownerId(currentUser), id, request.reason())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinanceCategoryResponse>> getById(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeCategoryService.getById(CurrentFinanceUser.ownerId(currentUser), id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FinanceCategoryResponse>>> getCategories(
            @RequestParam(required = false) FinanceCategoryType type,
            @RequestParam(required = false) FinanceCategoryStatus status,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(financeCategoryService.getCategories(
                CurrentFinanceUser.ownerId(currentUser),
                type,
                status,
                PageableLimits.normalize(pageable)
        ))));
    }
}
