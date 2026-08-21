package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.AdjustTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentRequest;
import com.lifebalance.resourcecapital.dto.CapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.PageResponseDTO;
import com.lifebalance.resourcecapital.dto.TimeCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Capital Adjustments", description = "Capital adjustment APIs")
@RestController
@RequestMapping("/api/v1/capital-adjustments")
public class CapitalAdjustmentController {

    private final CapitalAdjustmentService capitalAdjustmentService;

    public CapitalAdjustmentController(CapitalAdjustmentService capitalAdjustmentService) {
        this.capitalAdjustmentService = capitalAdjustmentService;
    }

    @Operation(summary = "Create capital adjustment", description = "Create an increase, decrease, or override adjustment for an active capital cycle.")
    @PostMapping
    public ResponseEntity<ApiResponse<CapitalAdjustmentResponse>> create(
            @Valid @RequestBody CapitalAdjustmentRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalAdjustmentResponse response = capitalAdjustmentService.adjustCapital(ownerId, request);
        URI location = URI.create("/api/v1/capital-adjustments/" + response.id());

        return ResponseEntity.created(location).body(ApiResponse.success(response));
    }

    @Operation(summary = "Adjust time capital", description = "Increase or decrease the planned time capital of a capital cycle.")
    @PostMapping("/time")
    public ResponseEntity<ApiResponse<TimeCapitalAdjustmentResponse>> adjustTimeCapital(
            @RequestParam UUID cycleId,
            @Valid @RequestBody AdjustTimeCapitalRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser) {
        UUID ownerId = resolveOwnerId(currentUser);

        TimeCapitalAdjustmentResponse response = capitalAdjustmentService.adjustTimeCapital(
                ownerId,
                cycleId,
                request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Adjust money capital", description = "Increase or decrease money capital of a capital cycle using the cycle currency.")
    @PostMapping("/money")
    public ResponseEntity<ApiResponse<MoneyCapitalAdjustmentResponse>> adjustMoneyCapital(
            @RequestParam UUID cycleId,
            @Valid @RequestBody AdjustMoneyCapitalRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser) {
        UUID ownerId = resolveOwnerId(currentUser);

        MoneyCapitalAdjustmentResponse response = capitalAdjustmentService.adjustMoneyCapital(
                ownerId,
                cycleId,
                request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "List capital adjustments", description = "Get paginated adjustment history for the authenticated user by cycle and optional capital type.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<CapitalAdjustmentResponse>>> getAdjustments(
            @RequestParam(required = false) UUID capitalCycleId,
            @RequestParam(required = false) CapitalKind capitalType,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser) {
        UUID ownerId = resolveOwnerId(currentUser);
        Page<CapitalAdjustmentResponse> adjustments = capitalAdjustmentService.getAdjustments(
                ownerId,
                capitalCycleId,
                capitalType,
                PageableLimits.normalize(pageable));

        return ResponseEntity.ok(ApiResponse.success(PageResponseDTO.from(adjustments)));
    }

    private UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
