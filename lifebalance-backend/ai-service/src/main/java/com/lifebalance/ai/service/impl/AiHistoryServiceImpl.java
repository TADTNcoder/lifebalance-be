package com.lifebalance.ai.service.impl;

import com.lifebalance.ai.domain.AiHistoryActionType;
import com.lifebalance.ai.dto.AiHistoryResponse;
import com.lifebalance.ai.repository.AiHistoryRepository;
import com.lifebalance.ai.service.AiHistoryService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AiHistoryServiceImpl implements AiHistoryService {

    private final AiHistoryRepository historyRepository;
    private final AiMapper mapper;

    AiHistoryServiceImpl(AiHistoryRepository historyRepository, AiMapper mapper) {
        this.historyRepository = historyRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiHistoryResponse> search(
            UUID ownerId,
            AiHistoryActionType actionType,
            UUID conversationId,
            UUID recommendationId,
            UUID insightId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        AiConversationServiceImpl.validateOwner(ownerId);
        AiConversationServiceImpl.validateOffsetRange(from, to);
        return historyRepository.search(ownerId, actionType, conversationId, recommendationId, insightId, from, to, pageable)
                .map(mapper::toResponse);
    }
}
