package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.resourcecapital.service.CapitalBalanceService;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.dto.AvailableCapitalResponse;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.service.mapper.AvailableCapitalMapper;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Available Capital", description = "Available Capital APIs")
@RestController
@RequestMapping("/api/v1/capital-cycles")
public class AvailableCapitalController {

    private final CapitalBalanceService capitalBalanceService;
    private final AvailableCapitalMapper availableCapitalMapper;

    public AvailableCapitalController(
            CapitalBalanceService capitalBalanceService,
            AvailableCapitalMapper availableCapitalMapper
    ) {
        this.capitalBalanceService = capitalBalanceService;
        this.availableCapitalMapper = availableCapitalMapper;
    }

    @Operation(
            summary = "Get available capital",
            description = "Get available time and money capital for an authenticated user's capital cycle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available capital returned"),
            @ApiResponse(responseCode = "400", description = "Request parameter has invalid format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found")
    })
    @GetMapping("/{cycleId}/available-capital")
    public ResponseEntity<AvailableCapitalResponse> getAvailableCapital(
            @PathVariable UUID cycleId,
            @RequestParam(required = false) CapitalKind type,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalBalanceResponse balance = capitalBalanceService.getCycleBalance(ownerId, cycleId);

        return ResponseEntity.ok(availableCapitalMapper.toAvailableCapitalResponse(balance, type));
    }

    private UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
