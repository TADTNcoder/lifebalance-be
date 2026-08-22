package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalallocation.AllocationStatus;
import com.lifebalance.resourcecapital.dto.AllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalAllocationChangeRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationReleaseRequest;
import com.lifebalance.resourcecapital.dto.CapitalAllocationResponse;
import com.lifebalance.resourcecapital.dto.CapitalReallocationRequest;
import com.lifebalance.resourcecapital.dto.CreateCapitalAllocationRequest;
import com.lifebalance.resourcecapital.dto.OverAllocationConfirmationResponse;
import com.lifebalance.resourcecapital.dto.PageResponseDTO;
import com.lifebalance.resourcecapital.service.CapitalAllocationService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Capital Allocations", description = "Capital allocation, reallocation, and release APIs")
@RestController
@RequestMapping("/api/v1/capital-allocations")
public class CapitalAllocationController {

    private final CapitalAllocationService capitalAllocationService;

    public CapitalAllocationController(CapitalAllocationService capitalAllocationService) {
        this.capitalAllocationService = capitalAllocationService;
    }

    @Operation(
            summary = "Allocate capital",
            description = "Allocate time or money capital from an active cycle to a task, task catalog, or project target."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<AllocationResponse>> allocate(
            @Valid @RequestBody CreateCapitalAllocationRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        AllocationResponse response = capitalAllocationService.allocateCapital(ownerId, request);

        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @Operation(
            summary = "Prepare over-allocation confirmation",
            description = "Calculate whether an allocation would over-allocate capital and return the confirmation key required for a confirmed allocation retry."
    )
    @PostMapping("/over-allocation-confirmation")
    public ResponseEntity<ApiResponse<OverAllocationConfirmationResponse>> prepareOverAllocationConfirmation(
            @Valid @RequestBody CreateCapitalAllocationRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        OverAllocationConfirmationResponse response =
                capitalAllocationService.prepareOverAllocationConfirmation(ownerId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Reallocate capital",
            description = "Move allocated capital between task targets or existing allocations in the same active cycle."
    )
    @PostMapping("/reallocate")
    public ResponseEntity<ApiResponse<AllocationResponse>> reallocate(
            @Valid @RequestBody CapitalReallocationRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        AllocationResponse response = capitalAllocationService.reallocateCapital(ownerId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Change allocation amount",
            description = "Change an existing active allocation amount without overwriting its previous history."
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AllocationResponse>> changeAllocation(
            @PathVariable UUID id,
            @Valid @RequestBody CapitalAllocationChangeRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        AllocationResponse response = capitalAllocationService.changeAllocation(ownerId, id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "Release capital",
            description = "Release allocated capital from an existing allocation back to the active cycle pool."
    )
    @PostMapping("/{id}/release")
    public ResponseEntity<ApiResponse<AllocationResponse>> release(
            @PathVariable UUID id,
            @Valid @RequestBody CapitalAllocationReleaseRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        AllocationResponse response = capitalAllocationService.releaseCapital(ownerId, id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "List capital allocations",
            description = "Get paginated capital allocations filtered by cycle, task, capital type, or status."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<CapitalAllocationResponse>>> getAllocations(
            @RequestParam(required = false) UUID capitalCycleId,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) CapitalKind capitalType,
            @RequestParam(required = false) AllocationStatus status,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        Page<CapitalAllocationResponse> allocations = capitalAllocationService.getAllocations(
                ownerId,
                capitalCycleId,
                taskId,
                capitalType,
                status,
                PageableLimits.normalize(pageable)
        );

        return ResponseEntity.ok(ApiResponse.success(PageResponseDTO.from(allocations)));
    }

    private UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
