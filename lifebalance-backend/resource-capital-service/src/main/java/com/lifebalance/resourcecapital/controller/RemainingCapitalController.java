package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.dto.CapitalBalanceResponse;
import com.lifebalance.resourcecapital.dto.RemainingCapitalResponse;
import com.lifebalance.resourcecapital.service.CapitalBalanceService;
import com.lifebalance.resourcecapital.service.mapper.RemainingCapitalMapper;
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

@Tag(name = "Remaining Capital", description = "Remaining Capital APIs")
@RestController
@RequestMapping("/api/v1/capital-cycles")
public class RemainingCapitalController {

    private final CapitalBalanceService capitalBalanceService;
    private final RemainingCapitalMapper remainingCapitalMapper;

    public RemainingCapitalController(
            CapitalBalanceService capitalBalanceService,
            RemainingCapitalMapper remainingCapitalMapper
    ) {
        this.capitalBalanceService = capitalBalanceService;
        this.remainingCapitalMapper = remainingCapitalMapper;
    }

    @Operation(
            summary = "Get remaining capital",
            description = "Get remaining time and money capital after current allocations for an authenticated user's capital cycle."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Remaining capital returned"),
            @ApiResponse(responseCode = "400", description = "Request parameter has invalid format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Capital cycle not found")
    })
    @GetMapping("/{cycleId}/remaining-capital")
    public ResponseEntity<RemainingCapitalResponse> getRemainingCapital(
            @PathVariable UUID cycleId,
            @RequestParam(required = false) CapitalKind type,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        CapitalBalanceResponse balance = capitalBalanceService.getCycleBalance(ownerId, cycleId);

        return ResponseEntity.ok(remainingCapitalMapper.toRemainingCapitalResponse(balance, type));
    }

    private UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
