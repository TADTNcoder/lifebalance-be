package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.resourcecapital.dto.SetupTimeCapitalRequest;
import com.lifebalance.resourcecapital.dto.TimeCapitalResponse;
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

@Tag(name = "Time Capital", description = "Time Capital APIs")
@RestController
@RequestMapping("/api/v1/capital-cycles")
public class TimeCapitalController {

    private final CapitalService capitalService;

    public TimeCapitalController(CapitalService capitalService) {
        this.capitalService = capitalService;
    }

    @Operation(
            summary = "Setup time capital",
            description = "Initialize the authenticated user's time capital for an editable capital cycle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Time capital initialized"),
            @ApiResponse(responseCode = "400", description = "Request validation failed or cycle status is invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found"),
            @ApiResponse(responseCode = "409", description = "Time capital already exists")
    })
    @PostMapping("/{cycleId}/time-capital")
    public ResponseEntity<TimeCapitalResponse> setup(
            @PathVariable UUID cycleId,
            @Valid @RequestBody SetupTimeCapitalRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        TimeCapitalResponse response = capitalService.setupTimeCapital(ownerId, cycleId, request);
        URI location = URI.create("/api/v1/capital-cycles/" + cycleId + "/time-capital");

        return ResponseEntity.created(location).body(response);
    }

    private UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
