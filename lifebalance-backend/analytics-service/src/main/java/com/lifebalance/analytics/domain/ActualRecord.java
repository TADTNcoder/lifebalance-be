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
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "actual_records", schema = "analytics")
public class ActualRecord {

    static final int NOTE_MAX_LENGTH = 1000;
    static final int SOURCE_MAX_LENGTH = 64;

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

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "tag_ids", columnDefinition = "TEXT")
    private String tagIds;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 32)
    private ActualRecordType recordType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ActualRecordStatus status;

    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    @Column(name = "actual_cost", precision = 19, scale = 4)
    private BigDecimal actualCost;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "actual_date", nullable = false)
    private LocalDate actualDate;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(length = NOTE_MAX_LENGTH)
    private String note;

    @Column(length = SOURCE_MAX_LENGTH)
    private String source;

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

    protected ActualRecord() {
    }

    public static ActualRecord create(
            UUID ownerId,
            UUID actorId,
            ActualRecordType recordType,
            UUID taskId,
            UUID capitalCycleId,
            UUID categoryId,
            String tagIds,
            Integer actualMinutes,
            BigDecimal actualCost,
            String currencyCode,
            LocalDate actualDate,
            String note,
            String source
    ) {
        validatePeriodDate(actualDate);
        ActualRecord actualRecord = new ActualRecord();
        actualRecord.ownerId = requireUuid(ownerId, "ownerId is required.");
        actualRecord.actorId = actorId;
        actualRecord.taskId = requireUuid(taskId, "taskId is required.");
        actualRecord.capitalCycleId = capitalCycleId;
        actualRecord.categoryId = categoryId;
        actualRecord.tagIds = normalizeText(tagIds, 2000);
        actualRecord.recordType = requireRecordType(recordType);
        actualRecord.status = ActualRecordStatus.ACTIVE;
        actualRecord.actualDate = actualDate;
        actualRecord.recordedAt = OffsetDateTime.now();
        actualRecord.note = normalizeText(note, NOTE_MAX_LENGTH);
        actualRecord.source = normalizeText(source, SOURCE_MAX_LENGTH);
        actualRecord.createdBy = actorId;
        actualRecord.updatedBy = actorId;
        actualRecord.applyMeasurement(actualMinutes, actualCost, currencyCode);
        return actualRecord;
    }

    public void update(
            UUID actorId,
            ActualRecordType recordType,
            UUID taskId,
            UUID capitalCycleId,
            UUID categoryId,
            String tagIds,
            Integer actualMinutes,
            BigDecimal actualCost,
            String currencyCode,
            LocalDate actualDate,
            String note,
            String source
    ) {
        ensureActive();
        validatePeriodDate(actualDate);
        this.actorId = actorId;
        this.recordType = requireRecordType(recordType);
        this.taskId = requireUuid(taskId, "taskId is required.");
        this.capitalCycleId = capitalCycleId;
        this.categoryId = categoryId;
        this.tagIds = normalizeText(tagIds, 2000);
        this.actualDate = actualDate;
        this.note = normalizeText(note, NOTE_MAX_LENGTH);
        this.source = normalizeText(source, SOURCE_MAX_LENGTH);
        this.updatedBy = actorId;
        applyMeasurement(actualMinutes, actualCost, currencyCode);
    }

    public void archive(UUID actorId) {
        ensureActive();
        status = ActualRecordStatus.ARCHIVED;
        archivedAt = OffsetDateTime.now();
        updatedBy = actorId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        recordedAt = recordedAt == null ? now : recordedAt;
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = updatedAt == null ? now : updatedAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public static String normalizeText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw AnalyticsExceptions.textTooLong(maxLength);
        }
        return normalized;
    }

    public static String normalizeCurrency(String currencyCode) {
        String normalized = normalizeText(currencyCode, 3);
        if (normalized == null || normalized.length() != 3) {
            throw AnalyticsExceptions.invalidCurrency(currencyCode);
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private void applyMeasurement(Integer actualMinutes, BigDecimal actualCost, String currencyCode) {
        switch (recordType) {
            case TIME -> {
                this.actualMinutes = requireNonNegative(actualMinutes, "actualMinutes must be greater than or equal to zero.");
                if (actualCost != null || normalizeText(currencyCode, 3) != null) {
                    throw AnalyticsExceptions.invalidRequest("TIME actual record must not include actualCost or currencyCode.");
                }
                this.actualCost = null;
                this.currencyCode = null;
            }
            case MONEY -> {
                this.actualMinutes = null;
                this.actualCost = requireNonNegative(actualCost, "actualCost must be greater than or equal to zero.");
                this.currencyCode = normalizeCurrency(currencyCode);
            }
            case TIME_AND_MONEY -> {
                this.actualMinutes = requireNonNegative(actualMinutes, "actualMinutes must be greater than or equal to zero.");
                this.actualCost = requireNonNegative(actualCost, "actualCost must be greater than or equal to zero.");
                this.currencyCode = normalizeCurrency(currencyCode);
            }
        }
    }

    private static ActualRecordType requireRecordType(ActualRecordType recordType) {
        if (recordType == null) {
            throw AnalyticsExceptions.invalidRequest("recordType is required.");
        }
        return recordType;
    }

    private static UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
        return value;
    }

    private static Integer requireNonNegative(Integer value, String message) {
        if (value == null || value < 0) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
        return value;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
        return value;
    }

    private static void validatePeriodDate(LocalDate actualDate) {
        if (actualDate == null) {
            throw AnalyticsExceptions.invalidRequest("actualDate is required.");
        }
    }

    private void ensureActive() {
        if (status != ActualRecordStatus.ACTIVE) {
            throw AnalyticsExceptions.invalidState(id, String.valueOf(status));
        }
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getTagIds() {
        return tagIds;
    }

    public ActualRecordType getRecordType() {
        return recordType;
    }

    public ActualRecordStatus getStatus() {
        return status;
    }

    public Integer getActualMinutes() {
        return actualMinutes;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public LocalDate getActualDate() {
        return actualDate;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public String getNote() {
        return note;
    }

    public String getSource() {
        return source;
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
