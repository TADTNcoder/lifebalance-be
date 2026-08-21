package com.lifebalance.ai.repository;

import com.lifebalance.ai.domain.AiPriority;
import com.lifebalance.ai.domain.AiRecommendation;
import com.lifebalance.ai.domain.AiRecommendationStatus;
import com.lifebalance.ai.domain.AiRecommendationType;
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

public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, UUID> {

    Optional<AiRecommendation> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT recommendation
            FROM AiRecommendation recommendation
            WHERE recommendation.id = :recommendationId
              AND recommendation.ownerId = :ownerId
            """)
    Optional<AiRecommendation> findByIdAndOwnerIdForUpdate(
            @Param("recommendationId") UUID recommendationId,
            @Param("ownerId") UUID ownerId
    );

    @Query("""
            SELECT recommendation
            FROM AiRecommendation recommendation
            WHERE recommendation.ownerId = :ownerId
              AND (:recommendationType IS NULL OR recommendation.recommendationType = :recommendationType)
              AND (:status IS NULL OR recommendation.status = :status)
              AND (:priority IS NULL OR recommendation.priority = :priority)
              AND (:targetType IS NULL OR recommendation.targetType = :targetType)
              AND (:targetId IS NULL OR recommendation.targetId = :targetId)
              AND (:from IS NULL OR recommendation.generatedAt >= :from)
              AND (:to IS NULL OR recommendation.generatedAt <= :to)
            ORDER BY recommendation.generatedAt DESC, recommendation.id DESC
            """)
    Page<AiRecommendation> search(
            @Param("ownerId") UUID ownerId,
            @Param("recommendationType") AiRecommendationType recommendationType,
            @Param("status") AiRecommendationStatus status,
            @Param("priority") AiPriority priority,
            @Param("targetType") String targetType,
            @Param("targetId") UUID targetId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable
    );
}
