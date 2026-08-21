package com.lifebalance.ai.domain;

import com.lifebalance.ai.error.AiExceptions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_recommendations", schema = "ai")
public class AiRecommendation {

    public static final int TITLE_MAX_LENGTH = 200;
    public static final int DESCRIPTION_MAX_LENGTH = 2000;
    public static final int REFERENCE_TYPE_MAX_LENGTH = 64;
    public static final int REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type", nullable = false, length = 64)
    private AiRecommendationType recommendationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AiRecommendationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AiPriority priority;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(name = "source_type", length = REFERENCE_TYPE_MAX_LENGTH)
    private String sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "target_type", length = REFERENCE_TYPE_MAX_LENGTH)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "signal_summary", length = DESCRIPTION_MAX_LENGTH)
    private String signalSummary;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    @Column(name = "decision_reason", length = REASON_MAX_LENGTH)
    private String decisionReason;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected AiRecommendation() {
    }

    public static AiRecommendation generate(
            UUID ownerId,
            UUID actorId,
            AiRecommendationType recommendationType,
            AiPriority priority,
            String title,
            String description,
            String sourceType,
            UUID sourceId,
            String targetType,
            UUID targetId,
            BigDecimal confidenceScore,
            String signalSummary
    ) {
        validateReferencePair(sourceType, sourceId, "sourceType and sourceId must be provided together.");
        validateReferencePair(targetType, targetId, "targetType and targetId must be provided together.");
        AiRecommendation recommendation = new AiRecommendation();
        recommendation.ownerId = requireUuid(ownerId, "ownerId is required.");
        recommendation.actorId = actorId;
        recommendation.recommendationType = recommendationType == null ? AiRecommendationType.GENERAL : recommendationType;
        recommendation.status = AiRecommendationStatus.PENDING;
        recommendation.priority = priority == null ? AiPriority.MEDIUM : priority;
        recommendation.title = AiText.require(title, TITLE_MAX_LENGTH, "title is required.");
        recommendation.description = AiText.require(description, DESCRIPTION_MAX_LENGTH, "description is required.");
        recommendation.sourceType = AiText.normalize(sourceType, REFERENCE_TYPE_MAX_LENGTH);
        recommendation.sourceId = sourceId;
        recommendation.targetType = AiText.normalize(targetType, REFERENCE_TYPE_MAX_LENGTH);
        recommendation.targetId = targetId;
        recommendation.confidenceScore = normalizeConfidence(confidenceScore);
        recommendation.signalSummary = AiText.normalize(signalSummary, DESCRIPTION_MAX_LENGTH);
        recommendation.generatedAt = OffsetDateTime.now();
        recommendation.createdBy = actorId;
        recommendation.updatedBy = actorId;
        return recommendation;
    }

    public void apply(UUID actorId, String reason) {
        ensurePending();
        status = AiRecommendationStatus.APPLIED;
        decidedAt = OffsetDateTime.now();
        decisionReason = AiText.normalize(reason, REASON_MAX_LENGTH);
        updatedBy = actorId;
    }

    public void dismiss(UUID actorId, String reason) {
        ensurePending();
        status = AiRecommendationStatus.DISMISSED;
        decidedAt = OffsetDateTime.now();
        decisionReason = AiText.normalize(reason, REASON_MAX_LENGTH);
        updatedBy = actorId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        generatedAt = generatedAt == null ? now : generatedAt;
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = updatedAt == null ? now : updatedAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    private void ensurePending() {
        if (status != AiRecommendationStatus.PENDING) {
            throw AiExceptions.invalidState(id, String.valueOf(status));
        }
    }

    private static BigDecimal normalizeConfidence(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw AiExceptions.invalidRequest("confidenceScore must be between zero and one.");
        }
        return value;
    }

    private static void validateReferencePair(String type, UUID id, String message) {
        boolean hasType = AiText.normalize(type, REFERENCE_TYPE_MAX_LENGTH) != null;
        boolean hasId = id != null;
        if (hasType != hasId) {
            throw AiExceptions.invalidRequest(message);
        }
    }

    private static UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw AiExceptions.invalidRequest(message);
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public AiRecommendationType getRecommendationType() {
        return recommendationType;
    }

    public AiRecommendationStatus getStatus() {
        return status;
    }

    public AiPriority getPriority() {
        return priority;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public String getSignalSummary() {
        return signalSummary;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
