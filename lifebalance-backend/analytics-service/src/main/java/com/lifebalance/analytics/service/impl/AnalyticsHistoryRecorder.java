package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.ActualRecord;
import com.lifebalance.analytics.domain.AnalyticsHistory;
import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.domain.AnalyticsReport;
import com.lifebalance.analytics.domain.EvaluationResult;
import com.lifebalance.analytics.repository.AnalyticsHistoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class AnalyticsHistoryRecorder {

    private final AnalyticsHistoryRepository historyRepository;

    AnalyticsHistoryRecorder(AnalyticsHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    void recordActual(
            UUID ownerId,
            UUID actorId,
            AnalyticsHistoryActionType actionType,
            ActualRecord actualRecord,
            String oldValue,
            String newValue,
            String reason
    ) {
        historyRepository.save(AnalyticsHistory.record(
                ownerId,
                actorId,
                actionType,
                actualRecord,
                null,
                null,
                oldValue,
                newValue,
                reason
        ));
    }

    void recordEvaluation(
            UUID ownerId,
            UUID actorId,
            AnalyticsHistoryActionType actionType,
            EvaluationResult evaluationResult,
            String oldValue,
            String newValue,
            String reason
    ) {
        historyRepository.save(AnalyticsHistory.record(
                ownerId,
                actorId,
                actionType,
                null,
                evaluationResult,
                null,
                oldValue,
                newValue,
                reason
        ));
    }

    void recordReport(
            UUID ownerId,
            UUID actorId,
            AnalyticsHistoryActionType actionType,
            AnalyticsReport report,
            String oldValue,
            String newValue,
            String reason
    ) {
        historyRepository.save(AnalyticsHistory.record(
                ownerId,
                actorId,
                actionType,
                null,
                null,
                report,
                oldValue,
                newValue,
                reason
        ));
    }
}
