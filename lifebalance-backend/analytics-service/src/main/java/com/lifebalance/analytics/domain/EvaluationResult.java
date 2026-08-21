package com.lifebalance.analytics.domain;

import com.lifebalance.analytics.error.AnalyticsExceptions;
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
@Table(name = "evaluation_results", schema = "analytics")
public class EvaluationResult {

    static final int REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "capital_cycle_id")
    private UUID capitalCycleId;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "planned_minutes")
    private Integer plannedMinutes;

    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    @Column(name = "minute_variance")
    private Integer minuteVariance;

    @Column(name = "planned_cost", precision = 19, scale = 4)
    private BigDecimal plannedCost;

    @Column(name = "actual_cost", precision = 19, scale = 4)
    private BigDecimal actualCost;

    @Column(name = "cost_variance", precision = 19, scale = 4)
    private BigDecimal costVariance;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "efficiency_percent", precision = 9, scale = 4)
    private BigDecimal efficiencyPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EvaluationStatus status;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    @Column(length = REASON_MAX_LENGTH)
    private String reason;

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

    protected EvaluationResult() {
    }

    public static EvaluationResult create(
            UUID ownerId,
            UUID actorId,
            UUID taskId,
            UUID capitalCycleId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Integer plannedMinutes,
            Integer actualMinutes,
            Integer minuteVariance,
            BigDecimal plannedCost,
            BigDecimal actualCost,
            BigDecimal costVariance,
            String currencyCode,
            BigDecimal efficiencyPercent,
            EvaluationStatus status,
            String reason
    ) {
        validatePeriod(periodStart, periodEnd);
        validateNonNegative(plannedMinutes, "plannedMinutes must not be negative.");
        validateNonNegative(actualMinutes, "actualMinutes must not be negative.");
        validateNonNegative(plannedCost, "plannedCost must not be negative.");
        validateNonNegative(actualCost, "actualCost must not be negative.");
        if ((plannedCost != null || actualCost != null) && ActualRecord.normalizeText(currencyCode, 3) == null) {
            throw AnalyticsExceptions.invalidCurrency(currencyCode);
        }

        EvaluationResult result = new EvaluationResult();
        result.ownerId = requireUuid(ownerId, "ownerId is required.");
        result.actorId = actorId;
        result.taskId = requireUuid(taskId, "taskId is required.");
        result.capitalCycleId = capitalCycleId;
        result.periodStart = periodStart;
        result.periodEnd = periodEnd;
        result.plannedMinutes = plannedMinutes;
        result.actualMinutes = actualMinutes;
        result.minuteVariance = minuteVariance;
        result.plannedCost = plannedCost;
        result.actualCost = actualCost;
        result.costVariance = costVariance;
        result.currencyCode = plannedCost == null && actualCost == null ? null : ActualRecord.normalizeCurrency(currencyCode);
        result.efficiencyPercent = efficiencyPercent;
        result.status = requireStatus(status);
        result.generatedAt = OffsetDateTime.now();
        result.reason = ActualRecord.normalizeText(reason, REASON_MAX_LENGTH);
        result.createdBy = actorId;
        result.updatedBy = actorId;
        return result;
    }

    public void archive(UUID actorId) {
        if (status == EvaluationStatus.ARCHIVED) {
            throw AnalyticsExceptions.invalidState(id, String.valueOf(status));
        }
        status = EvaluationStatus.ARCHIVED;
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

    private static void validatePeriod(LocalDate periodStart, LocalDate periodEnd) {
        if (periodStart != null && periodEnd != null && periodStart.isAfter(periodEnd)) {
            throw AnalyticsExceptions.invalidPeriod("periodStart must be before or equal to periodEnd.");
        }
    }

    private static void validateNonNegative(Integer value, String message) {
        if (value != null && value < 0) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
    }

    private static void validateNonNegative(BigDecimal value, String message) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
    }

    private static UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
        return value;
    }

    private static EvaluationStatus requireStatus(EvaluationStatus status) {
        if (status == null) {
            throw AnalyticsExceptions.invalidRequest("status is required.");
        }
        return status;
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

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getCapitalCycleId() {
        return capitalCycleId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public Integer getPlannedMinutes() {
        return plannedMinutes;
    }

    public Integer getActualMinutes() {
        return actualMinutes;
    }

    public Integer getMinuteVariance() {
        return minuteVariance;
    }

    public BigDecimal getPlannedCost() {
        return plannedCost;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public BigDecimal getCostVariance() {
        return costVariance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getEfficiencyPercent() {
        return efficiencyPercent;
    }

    public EvaluationStatus getStatus() {
        return status;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public String getReason() {
        return reason;
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
