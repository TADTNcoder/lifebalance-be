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
@Table(name = "analytics_reports", schema = "analytics")
public class AnalyticsReport {

    static final int REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 32)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportDimension dimension;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "task_count", nullable = false)
    private Integer taskCount;

    @Column(name = "actual_record_count", nullable = false)
    private Integer actualRecordCount;

    @Column(name = "total_actual_minutes", nullable = false)
    private Integer totalActualMinutes;

    @Column(name = "total_actual_cost", precision = 19, scale = 4)
    private BigDecimal totalActualCost;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "average_efficiency_percent", precision = 9, scale = 4)
    private BigDecimal averageEfficiencyPercent;

    @Column(name = "variance_summary", columnDefinition = "TEXT")
    private String varianceSummary;

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

    protected AnalyticsReport() {
    }

    public static AnalyticsReport generate(
            UUID ownerId,
            UUID actorId,
            ReportType reportType,
            ReportDimension dimension,
            LocalDate periodStart,
            LocalDate periodEnd,
            int taskCount,
            int actualRecordCount,
            int totalActualMinutes,
            BigDecimal totalActualCost,
            String currencyCode,
            BigDecimal averageEfficiencyPercent,
            String varianceSummary,
            String reason
    ) {
        validatePeriod(periodStart, periodEnd);
        validateCount(taskCount, "taskCount must not be negative.");
        validateCount(actualRecordCount, "actualRecordCount must not be negative.");
        validateCount(totalActualMinutes, "totalActualMinutes must not be negative.");
        if (totalActualCost != null && totalActualCost.compareTo(BigDecimal.ZERO) < 0) {
            throw AnalyticsExceptions.invalidRequest("totalActualCost must not be negative.");
        }

        AnalyticsReport report = new AnalyticsReport();
        report.ownerId = requireUuid(ownerId, "ownerId is required.");
        report.actorId = actorId;
        report.reportType = requireReportType(reportType);
        report.status = ReportStatus.GENERATED;
        report.dimension = requireDimension(dimension);
        report.periodStart = periodStart;
        report.periodEnd = periodEnd;
        report.taskCount = taskCount;
        report.actualRecordCount = actualRecordCount;
        report.totalActualMinutes = totalActualMinutes;
        report.totalActualCost = totalActualCost;
        report.currencyCode = totalActualCost == null ? null : ActualRecord.normalizeCurrency(currencyCode);
        report.averageEfficiencyPercent = averageEfficiencyPercent;
        report.varianceSummary = ActualRecord.normalizeText(varianceSummary, 4000);
        report.generatedAt = OffsetDateTime.now();
        report.reason = ActualRecord.normalizeText(reason, REASON_MAX_LENGTH);
        report.createdBy = actorId;
        report.updatedBy = actorId;
        return report;
    }

    public void archive(UUID actorId) {
        if (status == ReportStatus.ARCHIVED) {
            throw AnalyticsExceptions.invalidState(id, String.valueOf(status));
        }
        status = ReportStatus.ARCHIVED;
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
        if (periodStart == null || periodEnd == null) {
            throw AnalyticsExceptions.invalidPeriod("periodStart and periodEnd are required.");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw AnalyticsExceptions.invalidPeriod("periodStart must be before or equal to periodEnd.");
        }
    }

    private static void validateCount(int value, String message) {
        if (value < 0) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
    }

    private static UUID requireUuid(UUID value, String message) {
        if (value == null) {
            throw AnalyticsExceptions.invalidRequest(message);
        }
        return value;
    }

    private static ReportType requireReportType(ReportType reportType) {
        if (reportType == null) {
            throw AnalyticsExceptions.invalidRequest("reportType is required.");
        }
        return reportType;
    }

    private static ReportDimension requireDimension(ReportDimension dimension) {
        if (dimension == null) {
            throw AnalyticsExceptions.invalidRequest("dimension is required.");
        }
        return dimension;
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

    public ReportType getReportType() {
        return reportType;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public ReportDimension getDimension() {
        return dimension;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public Integer getActualRecordCount() {
        return actualRecordCount;
    }

    public Integer getTotalActualMinutes() {
        return totalActualMinutes;
    }

    public BigDecimal getTotalActualCost() {
        return totalActualCost;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getAverageEfficiencyPercent() {
        return averageEfficiencyPercent;
    }

    public String getVarianceSummary() {
        return varianceSummary;
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
