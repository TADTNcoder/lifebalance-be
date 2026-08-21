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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_insights", schema = "ai")
public class AiInsight {

    public static final int TITLE_MAX_LENGTH = 200;
    public static final int SUMMARY_MAX_LENGTH = 2000;
    public static final int REFERENCE_TYPE_MAX_LENGTH = 64;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "insight_type", nullable = false, length = 64)
    private AiInsightType insightType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AiInsightSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AiInsightStatus status;

    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(nullable = false, length = SUMMARY_MAX_LENGTH)
    private String summary;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "reference_type", length = REFERENCE_TYPE_MAX_LENGTH)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "confidence_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "signal_summary", length = SUMMARY_MAX_LENGTH)
    private String signalSummary;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "archived_at")
    private OffsetDateTime archivedAt;

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

    protected AiInsight() {
    }

    public static AiInsight generate(
            UUID ownerId,
            UUID actorId,
            AiInsightType insightType,
            AiInsightSeverity severity,
            String title,
            String summary,
            LocalDate periodStart,
            LocalDate periodEnd,
            String referenceType,
            UUID referenceId,
            BigDecimal confidenceScore,
            String signalSummary
    ) {
        validatePeriod(periodStart, periodEnd);
        validateReferencePair(referenceType, referenceId);
        AiInsight insight = new AiInsight();
        insight.ownerId = requireUuid(ownerId, "ownerId is required.");
        insight.actorId = actorId;
        insight.insightType = insightType == null ? AiInsightType.GENERAL : insightType;
        insight.severity = severity == null ? AiInsightSeverity.INFO : severity;
        insight.status = AiInsightStatus.ACTIVE;
        insight.title = AiText.require(title, TITLE_MAX_LENGTH, "title is required.");
        insight.summary = AiText.require(summary, SUMMARY_MAX_LENGTH, "summary is required.");
        insight.periodStart = periodStart;
        insight.periodEnd = periodEnd;
        insight.referenceType = AiText.normalize(referenceType, REFERENCE_TYPE_MAX_LENGTH);
        insight.referenceId = referenceId;
        insight.confidenceScore = normalizeConfidence(confidenceScore);
        insight.signalSummary = AiText.normalize(signalSummary, SUMMARY_MAX_LENGTH);
        insight.generatedAt = OffsetDateTime.now();
        insight.createdBy = actorId;
        insight.updatedBy = actorId;
        return insight;
    }

    public void archive(UUID actorId) {
        if (status == AiInsightStatus.ARCHIVED) {
            throw AiExceptions.invalidState(id, String.valueOf(status));
        }
        status = AiInsightStatus.ARCHIVED;
        archivedAt = OffsetDateTime.now();
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

    public static void validatePeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart != null && periodEnd != null && periodStart.isAfter(periodEnd)) {
            throw AiExceptions.invalidPeriod("periodStart must be before or equal to periodEnd.");
        }
    }

    private static void validateReferencePair(String referenceType, UUID referenceId) {
        boolean hasReferenceType = AiText.normalize(referenceType, REFERENCE_TYPE_MAX_LENGTH) != null;
        boolean hasReferenceId = referenceId != null;
        if (hasReferenceType != hasReferenceId) {
            throw AiExceptions.invalidRequest("referenceType and referenceId must be provided together.");
        }
    }

    private static BigDecimal normalizeConfidence(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            throw AiExceptions.invalidRequest("confidenceScore must be between zero and one.");
        }
        return value;
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

    public AiInsightType getInsightType() {
        return insightType;
    }

    public AiInsightSeverity getSeverity() {
        return severity;
    }

    public AiInsightStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
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

    public OffsetDateTime getArchivedAt() {
        return archivedAt;
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
