package com.lifebalance.analytics.dto;

import java.util.UUID;

public record PeriodComparisonResponse(
        UUID ownerId,
        TrackingEvaluationSummaryResponse baseline,
        TrackingEvaluationSummaryResponse comparison,
        EvaluationDeltaResponse delta
) {
}
