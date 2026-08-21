package com.lifebalance.ai.service;

import com.lifebalance.ai.domain.AiConversationStatus;
import com.lifebalance.ai.domain.AiIntent;
import com.lifebalance.ai.dto.AiConversationResponse;
import com.lifebalance.ai.dto.AiMessageResponse;
import com.lifebalance.ai.dto.AiReplyResponse;
import com.lifebalance.ai.dto.AskAiRequest;
import com.lifebalance.ai.dto.ReasonRequest;
import com.lifebalance.ai.dto.StartConversationRequest;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AiConversationService {

    AiReplyResponse start(UUID ownerId, StartConversationRequest request);

    AiReplyResponse ask(UUID ownerId, UUID conversationId, AskAiRequest request);

    AiConversationResponse getById(UUID ownerId, UUID conversationId);

    Page<AiConversationResponse> search(
            UUID ownerId,
            AiConversationStatus status,
            AiIntent intent,
            String contextType,
            UUID contextId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    Page<AiMessageResponse> messages(UUID ownerId, UUID conversationId, Pageable pageable);

    AiConversationResponse archive(UUID ownerId, UUID conversationId, ReasonRequest request);
}
