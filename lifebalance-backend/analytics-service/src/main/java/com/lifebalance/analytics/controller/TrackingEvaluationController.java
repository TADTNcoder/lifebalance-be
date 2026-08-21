package com.lifebalance.analytics.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.domain.TrendGranularity;
import com.lifebalance.analytics.dto.ComparePeriodsRequest;
import com.lifebalance.analytics.dto.EvaluationTrendPointResponse;
import com.lifebalance.analytics.dto.PageResponse;
import com.lifebalance.analytics.dto.PeriodComparisonResponse;
import com.lifebalance.analytics.dto.PlannedActualDetailResponse;
import com.lifebalance.analytics.dto.ResourceUtilizationResponse;
import com.lifebalance.analytics.dto.TrackingEvaluationSummaryResponse;
import com.lifebalance.analytics.service.TrackingEvaluationService;
import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/tracking-evaluation")
public class TrackingEvaluationController {

    private final TrackingEvaluationService trackingEvaluationService;

    public TrackingEvaluationController(TrackingEvaluationService trackingEvaluationService) {
        this.trackingEvaluationService = trackingEvaluationService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TrackingEvaluationSummaryResponse>> summary(
            @RequestParam(required = false) LocalDate periodStart,
            @RequestParam(required = false) LocalDate periodEnd,
            @RequestParam(required = false) String currencyCode,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(trackingEvaluationService.summary(
                CurrentAnalyticsUser.ownerId(currentUser),
                periodStart,
                periodEnd,
                currencyCode
        )));
    }

    @GetMapping("/planned-vs-actual")
    public ResponseEntity<ApiResponse<PageResponse<PlannedActualDetailResponse>>> plannedVsActual(
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) UUID capitalCycleId,
            @RequestParam(required = false) EvaluationStatus status,
            @RequestParam(required = false) LocalDate periodStart,
            @RequestParam(required = false) LocalDate periodEnd,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(trackingEvaluationService.plannedVsActual(
                CurrentAnalyticsUser.ownerId(currentUser),
                taskId,
                capitalCycleId,
                status,
                periodStart,
                periodEnd,
                PageableLimits.normalize(pageable)
        ))));
    }

    @GetMapping("/resource-utilization")
    public ResponseEntity<ApiResponse<List<ResourceUtilizationResponse>>> resourceUtilization(
            @RequestParam(required = false) LocalDate periodStart,
            @RequestParam(required = false) LocalDate periodEnd,
            @RequestParam(required = false) String currencyCode,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(trackingEvaluationService.resourceUtilization(
                CurrentAnalyticsUser.ownerId(currentUser),
                periodStart,
                periodEnd,
                currencyCode
        )));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<List<EvaluationTrendPointResponse>>> trend(
            @RequestParam LocalDate periodStart,
            @RequestParam LocalDate periodEnd,
            @RequestParam(required = false) TrendGranularity granularity,
            @RequestParam(required = false) String currencyCode,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(trackingEvaluationService.trend(
                CurrentAnalyticsUser.ownerId(currentUser),
                periodStart,
                periodEnd,
                granularity,
                currencyCode
        )));
    }

    @PostMapping("/compare-periods")
    public ResponseEntity<ApiResponse<PeriodComparisonResponse>> comparePeriods(
            @Valid @RequestBody ComparePeriodsRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(trackingEvaluationService.comparePeriods(
                CurrentAnalyticsUser.ownerId(currentUser),
                request
        )));
    }
}
