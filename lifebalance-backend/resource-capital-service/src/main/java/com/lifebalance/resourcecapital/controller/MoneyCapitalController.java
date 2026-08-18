package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.resourcecapital.dto.AdjustMoneyCapitalRequest;
import com.lifebalance.resourcecapital.dto.MoneyCapitalAdjustmentResponse;
import com.lifebalance.resourcecapital.dto.MoneyCapitalResponse;
import com.lifebalance.resourcecapital.dto.SetupMoneyCapitalRequest;
import com.lifebalance.resourcecapital.service.CapitalAdjustmentService;
import com.lifebalance.resourcecapital.service.CapitalService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Money Capital", description = "Money Capital APIs")
@RestController
@RequestMapping("/api/v1/capital-cycles")
public class MoneyCapitalController {

    private final CapitalService capitalService;
    private final CapitalAdjustmentService capitalAdjustmentService;

    public MoneyCapitalController(
            CapitalService capitalService,
            CapitalAdjustmentService capitalAdjustmentService
    ) {
        this.capitalService = capitalService;
        this.capitalAdjustmentService = capitalAdjustmentService;
    }

    @Operation(
            summary = "Setup money capital",
            description = "Initialize the authenticated user's money capital for an editable capital cycle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Money capital initialized"),
            @ApiResponse(responseCode = "400", description = "Request validation failed or cycle status is invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found"),
            @ApiResponse(responseCode = "409", description = "Money capital already exists")
    })
    @PostMapping("/{cycleId}/money-capital")
    public ResponseEntity<MoneyCapitalResponse> setup(
            @PathVariable UUID cycleId,
            @Valid @RequestBody SetupMoneyCapitalRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        MoneyCapitalResponse response = capitalService.setupMoneyCapital(ownerId, cycleId, request);
        URI location = URI.create("/api/v1/capital-cycles/" + cycleId + "/money-capital");

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Adjust money capital",
            description = "Increase or decrease money capital for an adjustable cycle using the cycle currency."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Money capital adjusted"),
            @ApiResponse(responseCode = "400", description = "Request validation failed or cycle status is invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Capital cycle or money capital not found"),
            @ApiResponse(responseCode = "409", description = "Over-allocation confirmation is required or not allowed")
    })
    @PostMapping("/{cycleId}/money-capital/adjustments")
    public ResponseEntity<MoneyCapitalAdjustmentResponse> adjust(
            @PathVariable UUID cycleId,
            @Valid @RequestBody AdjustMoneyCapitalRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        MoneyCapitalAdjustmentResponse response = capitalAdjustmentService.adjustMoneyCapital(
                ownerId,
                cycleId,
                request
        );

        return ResponseEntity.ok(response);
    }

    private UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
