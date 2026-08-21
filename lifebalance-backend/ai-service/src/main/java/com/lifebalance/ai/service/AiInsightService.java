package com.lifebalance.ai.service;

import com.lifebalance.ai.domain.AiInsightSeverity;
import com.lifebalance.ai.domain.AiInsightStatus;
import com.lifebalance.ai.domain.AiInsightType;
import com.lifebalance.ai.dto.AiInsightResponse;
import com.lifebalance.ai.dto.GenerateInsightRequest;
import com.lifebalance.ai.dto.ReasonRequest;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiInsightService {

    AiInsightResponse generate(UUID ownerId, GenerateInsightRequest request);

    AiInsightResponse getById(UUID ownerId, UUID insightId);

    Page<AiInsightResponse> search(
            UUID ownerId,
            AiInsightType insightType,
            AiInsightSeverity severity,
            AiInsightStatus status,
            String referenceType,
            UUID referenceId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Pageable pageable
    );

    AiInsightResponse archive(UUID ownerId, UUID insightId, ReasonRequest request);
}
