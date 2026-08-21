package com.lifebalance.ai.repository;

import com.lifebalance.ai.domain.AiMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {

    @Query("""
            SELECT message
            FROM AiMessage message
            WHERE message.conversation.id = :conversationId
              AND message.ownerId = :ownerId
            ORDER BY message.createdAt ASC, message.id ASC
            """)
    Page<AiMessage> findConversationMessages(
            @Param("ownerId") UUID ownerId,
            @Param("conversationId") UUID conversationId,
            Pageable pageable
    );
}
