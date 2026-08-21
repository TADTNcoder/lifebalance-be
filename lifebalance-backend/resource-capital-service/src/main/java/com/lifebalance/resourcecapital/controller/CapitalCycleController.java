package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.resourcecapital.dto.CapitalCycleResponse;
import com.lifebalance.resourcecapital.dto.CloseCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.ReopenCapitalCycleRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalRequest;
import com.lifebalance.resourcecapital.dto.TransferRemainingCapitalResponse;
import com.lifebalance.resourcecapital.dto.UpdateCapitalCycleRequest;
import com.lifebalance.resourcecapital.service.CapitalCycleService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Capital Cycles", description = "Capital Cycle APIs")
@RestController
@RequestMapping("/api/v1/capital-cycles")
public class CapitalCycleController {

    private final CapitalCycleService capitalCycleService;

    public CapitalCycleController(CapitalCycleService capitalCycleService) {
        this.capitalCycleService = capitalCycleService;
    }

    @Operation(
            summary = "Create capital cycle",
            description = "Create a new user-owned capital cycle in draft status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Capital cycle created"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<CapitalCycleResponse> create(
            @Valid @RequestBody CreateCapitalCycleRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalCycleResponse response = capitalCycleService.createCycle(ownerId, request);
        URI location = URI.create("/api/v1/capital-cycles/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "Update capital cycle",
            description = "Update editable information for an authenticated user's draft capital cycle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capital cycle updated"),
            @ApiResponse(responseCode = "400", description = "Request validation failed or cycle is not draft"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CapitalCycleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCapitalCycleRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalCycleResponse response = capitalCycleService.updateCycle(ownerId, id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Activate capital cycle",
            description = "Activate an authenticated user's capital cycle after validating ownership, status transition, and one-active-cycle rule."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capital cycle activated"),
            @ApiResponse(responseCode = "400", description = "Cycle status transition is not allowed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found"),
            @ApiResponse(responseCode = "409", description = "An active capital cycle already exists for the same cycle type")
    })
    @PostMapping("/{id}/activate")
    public ResponseEntity<CapitalCycleResponse> activate(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalCycleResponse response = capitalCycleService.activateCycle(ownerId, id);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Close capital cycle",
            description = "Close an authenticated user's active or reopened capital cycle with a business reason."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capital cycle closed"),
            @ApiResponse(responseCode = "400", description = "Request validation failed or cycle status transition is not allowed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found")
    })
    @PostMapping("/{id}/close")
    public ResponseEntity<CapitalCycleResponse> close(
            @PathVariable UUID id,
            @Valid @RequestBody CloseCapitalCycleRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalCycleResponse response = capitalCycleService.closeCycle(ownerId, id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Reopen capital cycle",
            description = "Reopen an authenticated user's closed capital cycle with a business reason."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capital cycle reopened"),
            @ApiResponse(responseCode = "400", description = "Request validation failed or cycle status transition is not allowed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found")
    })
    @PostMapping("/{id}/reopen")
    public ResponseEntity<CapitalCycleResponse> reopen(
            @PathVariable UUID id,
            @Valid @RequestBody ReopenCapitalCycleRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalCycleResponse response = capitalCycleService.reopenCycle(ownerId, id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Transfer remaining capital",
            description = "Transfer positive remaining capital from a closed source cycle to a valid future target cycle after explicit confirmation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remaining capital transferred"),
            @ApiResponse(responseCode = "400", description = "Request validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Source or target capital cycle not found"),
            @ApiResponse(responseCode = "409", description = "Transfer policy or balance validation failed")
    })
    @PostMapping("/{id}/transfer-remaining")
    public ResponseEntity<TransferRemainingCapitalResponse> transferRemaining(
            @PathVariable UUID id,
            @Valid @RequestBody TransferRemainingCapitalRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        TransferRemainingCapitalResponse response = capitalCycleService.transferRemainingCapital(
                ownerId,
                id,
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
