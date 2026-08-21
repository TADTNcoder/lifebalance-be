package com.lifebalance.task.model;

import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.RecurrenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_recurring_rules", schema = "task")
@SQLDelete(sql = """
        UPDATE task.task_recurring_rules
        SET deleted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """)
@SQLRestriction("deleted_at IS NULL")
public class TaskRecurringRule extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "policy_status", nullable = false, length = 32)
    @Builder.Default
    private OptionalFeaturePolicyStatus policyStatus = OptionalFeaturePolicyStatus.PENDING_APPROVAL;

    @NotNull
    @Column(name = "feature_enabled", nullable = false)
    @Builder.Default
    private Boolean featureEnabled = Boolean.FALSE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 32)
    private RecurrenceType recurrenceType;

    @NotNull
    @Positive
    @Column(name = "interval_count", nullable = false)
    @Builder.Default
    private Integer intervalCount = 1;

    @Size(max = 64)
    @Column(name = "days_of_week", length = 64)
    private String daysOfWeek;

    @NotNull
    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Positive
    @Column(name = "max_occurrences")
    private Integer maxOccurrences;

    @Size(max = 64)
    @Column(length = 64)
    private String timezone;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @PrePersist
    @PreUpdate
    void normalizeBeforeSave() {
        if (policyStatus == null) {
            policyStatus = OptionalFeaturePolicyStatus.PENDING_APPROVAL;
        }
        if (featureEnabled == null) {
            featureEnabled = Boolean.FALSE;
        }
        if (intervalCount == null) {
            intervalCount = 1;
        }
        if (timezone != null) {
            timezone = normalizeOptionalText(timezone, 64);
        }
        if (daysOfWeek != null) {
            daysOfWeek = normalizeOptionalText(daysOfWeek, 64);
        }
    }

    public void updateFrom(
            OptionalFeaturePolicyStatus policyStatus,
            Boolean featureEnabled,
            RecurrenceType recurrenceType,
            Integer intervalCount,
            String daysOfWeek,
            LocalDate startsOn,
            LocalDate endsOn,
            Integer maxOccurrences,
            String timezone,
            String reason,
            UUID actorId) {

        this.policyStatus = policyStatus;
        this.featureEnabled = featureEnabled;
        this.recurrenceType = recurrenceType;
        this.intervalCount = intervalCount == null ? 1 : intervalCount;
        this.daysOfWeek = normalizeOptionalText(daysOfWeek, 64);
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.maxOccurrences = maxOccurrences;
        this.timezone = normalizeOptionalText(timezone, 64);
        this.reason = reason;
        this.updatedBy = actorId;
    }

    public void disable(UUID actorId, String reason) {
        this.policyStatus = OptionalFeaturePolicyStatus.DISABLED;
        this.featureEnabled = Boolean.FALSE;
        this.reason = reason;
        this.updatedBy = actorId;
    }

    private String normalizeOptionalText(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("Task recurring rule text must not exceed " + maxLength + " characters.");
        }
        return normalized;
    }
}
