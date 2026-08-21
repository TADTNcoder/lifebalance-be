package com.lifebalance.analytics.service;

import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.domain.TrendGranularity;
import com.lifebalance.analytics.dto.ComparePeriodsRequest;
import com.lifebalance.analytics.dto.EvaluationTrendPointResponse;
import com.lifebalance.analytics.dto.PeriodComparisonResponse;
import com.lifebalance.analytics.dto.PlannedActualDetailResponse;
import com.lifebalance.analytics.dto.ResourceUtilizationResponse;
import com.lifebalance.analytics.dto.TrackingEvaluationSummaryResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrackingEvaluationService {

    TrackingEvaluationSummaryResponse summary(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String currencyCode
    );

    Page<PlannedActualDetailResponse> plannedVsActual(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            EvaluationStatus status,
            LocalDate periodStart,
            LocalDate periodEnd,
            Pageable pageable
    );

    List<ResourceUtilizationResponse> resourceUtilization(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String currencyCode
    );

    List<EvaluationTrendPointResponse> trend(
            UUID ownerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            TrendGranularity granularity,
            String currencyCode
    );

    PeriodComparisonResponse comparePeriods(UUID ownerId, ComparePeriodsRequest request);
}
