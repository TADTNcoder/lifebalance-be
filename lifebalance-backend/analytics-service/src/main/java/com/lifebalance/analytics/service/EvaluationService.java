package com.lifebalance.analytics.service;

import com.lifebalance.analytics.domain.EvaluationStatus;
import com.lifebalance.analytics.dto.EvaluateTaskRequest;
import com.lifebalance.analytics.dto.EvaluationResultResponse;
import com.lifebalance.analytics.dto.ReasonRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EvaluationService {

    EvaluationResultResponse evaluateTask(UUID ownerId, EvaluateTaskRequest request);

    EvaluationResultResponse archive(UUID ownerId, UUID evaluationId, ReasonRequest request);

    EvaluationResultResponse getById(UUID ownerId, UUID evaluationId);

    Page<EvaluationResultResponse> search(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            EvaluationStatus status,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );
}
