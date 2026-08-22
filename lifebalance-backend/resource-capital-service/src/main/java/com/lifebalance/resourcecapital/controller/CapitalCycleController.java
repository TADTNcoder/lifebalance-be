package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
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
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            summary = "List capital cycles",
            description = "List authenticated user's capital cycles with optional type, status, period, pagination, and sorting filters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capital cycles returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Page<CapitalCycleResponse>> list(
            @RequestParam(required = false) CapitalCycleType type,
            @RequestParam(required = false) CapitalCycleStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        Page<CapitalCycleResponse> response = capitalCycleService.listCycles(
                ownerId,
                type,
                status,
                fromDate,
                toDate,
                PageableLimits.normalize(pageable)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get active capital cycle",
            description = "Get the authenticated user's active capital cycle, optionally filtered by cycle type."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active capital cycle returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Active capital cycle not found")
    })
    @GetMapping("/active")
    public ResponseEntity<CapitalCycleResponse> active(
            @RequestParam(required = false) CapitalCycleType type,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);

        return ResponseEntity.of(capitalCycleService.getActiveCycle(ownerId, type));
    }

    @Operation(
            summary = "Get capital cycle detail",
            description = "Get a single authenticated user-owned capital cycle by id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Capital cycle returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CapitalCycleResponse> get(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);

        return ResponseEntity.ok(capitalCycleService.getCycle(ownerId, id));
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
            summary = "Delete capital cycle",
            description = "Delete an authenticated user's draft capital cycle only when no dependent capital records exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Capital cycle deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found"),
            @ApiResponse(responseCode = "409", description = "Capital cycle cannot be deleted")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        capitalCycleService.deleteCycle(ownerId, id);

        return ResponseEntity.noContent().build();
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
