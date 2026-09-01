package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.dto.AutomaticEvaluationBaselineRequest;
import java.util.UUID;

interface AutomaticEvaluationService {

    void evaluateAfterActualChange(
            UUID ownerId,
            AutomaticEvaluationTarget target,
            AutomaticEvaluationBaselineRequest requestedBaseline,
            String changeReason
    );
}
