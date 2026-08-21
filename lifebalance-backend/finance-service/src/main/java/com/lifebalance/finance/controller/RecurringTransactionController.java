package com.lifebalance.finance.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.finance.domain.RecurringTransactionStatus;
import com.lifebalance.finance.dto.CreateRecurringTransactionRequest;
import com.lifebalance.finance.dto.PageResponse;
import com.lifebalance.finance.dto.ReasonRequest;
import com.lifebalance.finance.dto.RecurringTransactionResponse;
import com.lifebalance.finance.dto.UpdateRecurringTransactionRequest;
import com.lifebalance.finance.service.RecurringTransactionService;
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
@RequestMapping("/api/finance/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> create(
            @Valid @RequestBody CreateRecurringTransactionRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        RecurringTransactionResponse response = recurringTransactionService.create(
                CurrentFinanceUser.ownerId(currentUser),
                request);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecurringTransactionRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                recurringTransactionService.update(CurrentFinanceUser.ownerId(currentUser), id, request)));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> pause(
            @PathVariable UUID id,
            @Valid @RequestBody ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                recurringTransactionService.pause(CurrentFinanceUser.ownerId(currentUser), id, request.reason())));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> resume(
            @PathVariable UUID id,
            @Valid @RequestBody ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                recurringTransactionService.resume(CurrentFinanceUser.ownerId(currentUser), id, request.reason())));
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> end(
            @PathVariable UUID id,
            @Valid @RequestBody ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                recurringTransactionService.end(CurrentFinanceUser.ownerId(currentUser), id, request.reason())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RecurringTransactionResponse>>> getRules(
            @RequestParam(required = false) RecurringTransactionStatus status,
            @RequestParam(required = false) LocalDate dueOnOrBefore,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(recurringTransactionService.getRules(
                CurrentFinanceUser.ownerId(currentUser),
                status,
                dueOnOrBefore,
                PageableLimits.normalize(pageable)
        ))));
    }
}
