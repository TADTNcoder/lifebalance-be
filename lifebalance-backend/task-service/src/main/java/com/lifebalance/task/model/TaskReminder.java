package com.lifebalance.task.model;

import com.lifebalance.task.model.enums.OptionalFeaturePolicyStatus;
import com.lifebalance.task.model.enums.ReminderChannel;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_reminders", schema = "task")
@SQLDelete(sql = """
        UPDATE task.task_reminders
        SET deleted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """)
@SQLRestriction("deleted_at IS NULL")
public class TaskReminder extends BaseAuditableEntity {

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
    @Column(name = "remind_at", nullable = false)
    private OffsetDateTime remindAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private ReminderChannel channel = ReminderChannel.IN_APP;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

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
        if (channel == null) {
            channel = ReminderChannel.IN_APP;
        }
    }

    public void updateFrom(
            OptionalFeaturePolicyStatus policyStatus,
            Boolean featureEnabled,
            OffsetDateTime remindAt,
            ReminderChannel channel,
            String message,
            String reason,
            UUID actorId) {

        this.policyStatus = policyStatus;
        this.featureEnabled = featureEnabled;
        this.remindAt = remindAt;
        this.channel = channel == null ? ReminderChannel.IN_APP : channel;
        this.message = message;
        this.reason = reason;
        this.updatedBy = actorId;
    }

    public void cancel(UUID actorId, String reason) {
        this.policyStatus = OptionalFeaturePolicyStatus.DISABLED;
        this.featureEnabled = Boolean.FALSE;
        this.cancelledAt = OffsetDateTime.now();
        this.reason = reason;
        this.updatedBy = actorId;
    }
}
