package com.lifebalance.finance.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.finance.domain.FinanceAccountStatus;
import com.lifebalance.finance.dto.CreateFinanceAccountRequest;
import com.lifebalance.finance.dto.FinanceAccountResponse;
import com.lifebalance.finance.dto.PageResponse;
import com.lifebalance.finance.dto.ReasonRequest;
import com.lifebalance.finance.dto.UpdateFinanceAccountRequest;
import com.lifebalance.finance.service.FinanceAccountService;
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
@RequestMapping("/api/finance/accounts")
public class FinanceAccountController {

    private final FinanceAccountService financeAccountService;

    public FinanceAccountController(FinanceAccountService financeAccountService) {
        this.financeAccountService = financeAccountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FinanceAccountResponse>> create(
            @Valid @RequestBody CreateFinanceAccountRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        FinanceAccountResponse response = financeAccountService.create(CurrentFinanceUser.ownerId(currentUser), request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FinanceAccountResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFinanceAccountRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeAccountService.update(CurrentFinanceUser.ownerId(currentUser), id, request)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<FinanceAccountResponse>> archive(
            @PathVariable UUID id,
            @Valid @RequestBody ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeAccountService.archive(CurrentFinanceUser.ownerId(currentUser), id, request.reason())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FinanceAccountResponse>> getById(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                financeAccountService.getById(CurrentFinanceUser.ownerId(currentUser), id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FinanceAccountResponse>>> getAccounts(
            @RequestParam(required = false) FinanceAccountStatus status,
            @RequestParam(required = false) String currencyCode,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(financeAccountService.getAccounts(
                CurrentFinanceUser.ownerId(currentUser),
                status,
                currencyCode,
                PageableLimits.normalize(pageable)
        ))));
    }
}
