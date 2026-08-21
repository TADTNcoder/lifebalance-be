package com.lifebalance.ai.repository;

import com.lifebalance.ai.domain.AiConversation;
import com.lifebalance.ai.domain.AiConversationStatus;
import com.lifebalance.ai.domain.AiIntent;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiConversationRepository extends JpaRepository<AiConversation, UUID> {

    Optional<AiConversation> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT conversation
            FROM AiConversation conversation
            WHERE conversation.id = :conversationId
              AND conversation.ownerId = :ownerId
            """)
    Optional<AiConversation> findByIdAndOwnerIdForUpdate(
            @Param("conversationId") UUID conversationId,
            @Param("ownerId") UUID ownerId
    );

    @Query("""
            SELECT conversation
            FROM AiConversation conversation
            WHERE conversation.ownerId = :ownerId
              AND (:status IS NULL OR conversation.status = :status)
              AND (:intent IS NULL OR conversation.intent = :intent)
              AND (:contextType IS NULL OR conversation.contextType = :contextType)
              AND (:contextId IS NULL OR conversation.contextId = :contextId)
              AND (:from IS NULL OR conversation.updatedAt >= :from)
              AND (:to IS NULL OR conversation.updatedAt <= :to)
            ORDER BY conversation.updatedAt DESC, conversation.id DESC
            """)
    Page<AiConversation> search(
            @Param("ownerId") UUID ownerId,
            @Param("status") AiConversationStatus status,
            @Param("intent") AiIntent intent,
            @Param("contextType") String contextType,
            @Param("contextId") UUID contextId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );
}
