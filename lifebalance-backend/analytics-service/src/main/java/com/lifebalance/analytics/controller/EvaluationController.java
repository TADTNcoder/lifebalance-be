package com.lifebalance.analytics.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.dto.EvaluateTaskRequest;
import com.lifebalance.analytics.dto.EvaluationResultResponse;
import com.lifebalance.analytics.dto.PageResponse;
import com.lifebalance.analytics.dto.ReasonRequest;
import com.lifebalance.analytics.service.EvaluationService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
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
@RequestMapping("/api/analytics/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluationResultResponse>> evaluateTask(
            @Valid @RequestBody EvaluateTaskRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.status(201).body(ApiResponse.success(evaluationService.evaluateTask(
                CurrentAnalyticsUser.ownerId(currentUser),
                request
        )));
    }

    @GetMapping("/{evaluationId}")
    public ResponseEntity<ApiResponse<EvaluationResultResponse>> getById(
            @PathVariable UUID evaluationId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(evaluationService.getById(
                CurrentAnalyticsUser.ownerId(currentUser),
                evaluationId
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EvaluationResultResponse>>> search(
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) UUID capitalCycleId,
            @RequestParam(required = false) EvaluationStatus status,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(evaluationService.search(
                CurrentAnalyticsUser.ownerId(currentUser),
                taskId,
                capitalCycleId,
                status,
                from,
                to,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{evaluationId}/archive")
    public ResponseEntity<ApiResponse<EvaluationResultResponse>> archive(
            @PathVariable UUID evaluationId,
            @Valid @RequestBody(required = false) ReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(evaluationService.archive(
                CurrentAnalyticsUser.ownerId(currentUser),
                evaluationId,
                request
        )));
    }
}
