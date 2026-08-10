package com.lifebalance.resourcecapital.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.dto.CapitalHistoryResponseDTO;
import com.lifebalance.resourcecapital.dto.CapitalSummaryResponseDTO;
import com.lifebalance.resourcecapital.dto.HistoryFilterRequest;
import com.lifebalance.resourcecapital.dto.PageResponseDTO;
import com.lifebalance.resourcecapital.service.CapitalHistoryService;
import com.lifebalance.resourcecapital.service.CapitalService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "Capital", description = "Capital history and summary APIs")
@RestController
@RequestMapping("/api/v1/capital")
public class CapitalController {

    private final CapitalHistoryService capitalHistoryService;
    private final CapitalService capitalService;

    public CapitalController(
            CapitalHistoryService capitalHistoryService,
            CapitalService capitalService
    ) {
        this.capitalHistoryService = capitalHistoryService;
        this.capitalService = capitalService;
    }

    @Operation(
            summary = "Get capital history",
            description = "Get paginated capital movement history for the authenticated user."
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponseDTO<CapitalHistoryResponseDTO>>> getHistory(
            @RequestParam(required = false) UUID capitalCycleId,
            @RequestParam(required = false) CapitalKind capitalType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant toDate,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);
        HistoryFilterRequest filter = new HistoryFilterRequest(
                capitalType,
                null,
                fromDate,
                toDate,
                null,
                null,
                null,
                null,
                null
        );
        Page<CapitalHistoryResponseDTO> history = capitalHistoryService.getHistory(
                ownerId,
                capitalCycleId,
                filter,
                pageable
        );

        return ResponseEntity.ok(ApiResponse.success(PageResponseDTO.from(history)));
    }

    @Operation(
            summary = "Get capital summary",
            description = "Get the current active capital cycle summary for the authenticated user."
    )
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CapitalSummaryResponseDTO>> getSummary(
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false)
            KeycloakUserPrincipal currentUser
    ) {
        UUID ownerId = resolveOwnerId(currentUser);

        return ResponseEntity.ok(ApiResponse.success(capitalService.getCapitalSummary(ownerId)));
    }

    private UUID resolveOwnerId(KeycloakUserPrincipal currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authenticated internal user id is required.");
        }
        return currentUser.userId();
    }
}
