package com.lifebalance.ai.service;

import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.dto.AiHistoryResponse;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiHistoryService {

    Page<AiHistoryResponse> search(
            UUID ownerId,
            AiHistoryActionType actionType,
            UUID conversationId,
            UUID recommendationId,
            UUID insightId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );
}
