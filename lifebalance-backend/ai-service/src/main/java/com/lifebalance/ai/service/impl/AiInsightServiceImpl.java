package com.lifebalance.ai.service.impl;

import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.domain.AiInsight;
import com.lifebalance.ai.domain.AiInsightSeverity;
import com.lifebalance.ai.domain.AiInsightStatus;
import com.lifebalance.ai.domain.AiInsightType;
import com.lifebalance.ai.dto.AiInsightResponse;
import com.lifebalance.ai.dto.GenerateInsightRequest;
import com.lifebalance.ai.dto.ReasonRequest;
import com.lifebalance.ai.error.AiExceptions;
import com.lifebalance.ai.repository.AiInsightRepository;
import com.lifebalance.ai.service.AiInsightService;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AiInsightServiceImpl implements AiInsightService {

    private final AiInsightRepository insightRepository;
    private final AiSuggestionEngine suggestionEngine;
    private final AiHistoryRecorder historyRecorder;
    private final AiMapper mapper;

    AiInsightServiceImpl(
            AiInsightRepository insightRepository,
            AiSuggestionEngine suggestionEngine,
            AiHistoryRecorder historyRecorder,
            AiMapper mapper
    ) {
        this.insightRepository = insightRepository;
        this.suggestionEngine = suggestionEngine;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AiInsightResponse generate(UUID ownerId, GenerateInsightRequest request) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiInsight.validatePeriod(request.periodStart(), request.periodEnd());
        AiSuggestionEngine.AiGeneratedInsight generatedInsight = suggestionEngine.insight(
                request.insightType(),
                request.signalSummary()
        );
        AiInsight insight = insightRepository.save(AiInsight.generate(
                ownerId,
                ownerId,
                generatedInsight.insightType(),
                generatedInsight.severity(),
                generatedInsight.title(),
                generatedInsight.summary(),
                request.periodStart(),
                request.periodEnd(),
                request.referenceType(),
                request.referenceId(),
                generatedInsight.confidenceScore(),
                request.signalSummary()
        ));
        historyRecorder.recordInsight(
                ownerId,
                ownerId,
                AiHistoryActionType.INSIGHT_GENERATED,
                insight,
                null,
                mapper.insightSnapshot(insight),
                "Generated from AI insight request"
        );
        return mapper.toResponse(insight);
    }

    @Override
    @Transactional(readOnly = true)
    public AiInsightResponse getById(UUID ownerId, UUID insightId) {
        AiConversationServiceImpl.validateOwner(ownerId);
        return insightRepository.findByIdAndOwnerId(insightId, ownerId)
                .map(mapper::toResponse)
                .orElseThrow(() -> AiExceptions.insightNotFound(insightId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiInsightResponse> search(
            UUID ownerId,
            AiInsightType insightType,
            AiInsightSeverity severity,
            AiInsightStatus status,
            String referenceType,
            UUID referenceId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Pageable pageable
    ) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiInsight.validatePeriod(periodStart, periodEnd);
        return insightRepository.search(
                ownerId,
                insightType,
                severity,
                status,
                referenceType,
                referenceId,
                periodStart,
                periodEnd,
                pageable
        ).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public AiInsightResponse archive(UUID ownerId, UUID insightId, ReasonRequest request) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiInsight insight = insightRepository.findByIdAndOwnerIdForUpdate(insightId, ownerId)
                .orElseThrow(() -> AiExceptions.insightNotFound(insightId));
        String oldSnapshot = mapper.insightSnapshot(insight);
        insight.archive(ownerId);
        historyRecorder.recordInsight(
                ownerId,
                ownerId,
                AiHistoryActionType.INSIGHT_ARCHIVED,
                insight,
                oldSnapshot,
                mapper.insightSnapshot(insight),
                request == null ? null : request.reason()
        );
        return mapper.toResponse(insight);
    }
}
