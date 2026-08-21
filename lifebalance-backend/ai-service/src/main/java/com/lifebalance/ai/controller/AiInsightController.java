package com.lifebalance.ai.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.ai.domain.AiInsightSeverity;
import com.lifebalance.ai.domain.AiInsightStatus;
import com.lifebalance.ai.domain.AiInsightType;
import com.lifebalance.ai.dto.AiInsightResponse;
import com.lifebalance.ai.dto.GenerateInsightRequest;
import com.lifebalance.ai.dto.PageResponse;
import com.lifebalance.ai.dto.ReasonRequest;
import com.lifebalance.ai.service.AiInsightService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
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
@RequestMapping("/api/ai/insights")
public class AiInsightController {

    private final AiInsightService insightService;

    public AiInsightController(AiInsightService insightService) {
        this.insightService = insightService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<AiInsightResponse>> generate(
            @Valid @RequestBody GenerateInsightRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(insightService.generate(
                CurrentAiUser.ownerId(currentUser),
                request
        )));
    }

    @GetMapping("/{insightId}")
    public ResponseEntity<ApiResponse<AiInsightResponse>> getById(
            @PathVariable UUID insightId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(insightService.getById(
                CurrentAiUser.ownerId(currentUser),
                insightId
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AiInsightResponse>>> search(
            @RequestParam(required = false) AiInsightType insightType,
            @RequestParam(required = false) AiInsightSeverity severity,
            @RequestParam(required = false) AiInsightStatus status,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) LocalDate periodStart,
            @RequestParam(required = false) LocalDate periodEnd,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(insightService.search(
                CurrentAiUser.ownerId(currentUser),
                insightType,
                severity,
                status,
                referenceType,
                referenceId,
                periodStart,
                periodEnd,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{insightId}/archive")
    public ResponseEntity<ApiResponse<AiInsightResponse>> archive(
            @PathVariable UUID insightId,
            @Valid @RequestBody(required = false) ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(insightService.archive(
                CurrentAiUser.ownerId(currentUser),
                insightId,
                request
        )));
    }
}
