package com.lifebalance.identity.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "support_tickets",
        schema = "identity",
        indexes = {
                @Index(name = "idx_identity_support_tickets_requester", columnList = "requester_id, created_at"),
                @Index(name = "idx_identity_support_tickets_assignee", columnList = "assignee_id, status"),
                @Index(name = "idx_identity_support_tickets_status", columnList = "status, priority, created_at"),
                @Index(name = "idx_identity_support_tickets_category", columnList = "category, created_at")
        }
)
@SQLDelete(sql = "UPDATE identity.support_tickets SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SupportTicket extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 32)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SupportTicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SupportTicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private SupportTicketCategory category;

    @Column(columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "escalation_reason", columnDefinition = "TEXT")
    private String escalationReason;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "reopened_at")
    private OffsetDateTime reopenedAt;

    @Column(name = "last_status_changed_at", nullable = false)
    private OffsetDateTime lastStatusChangedAt;

    @PrePersist
    @PreUpdate
    void applyDefaults() {
        title = trim(title);
        description = trim(description);
        resolution = trim(resolution);
        escalationReason = trim(escalationReason);
        if (status == null) {
            status = SupportTicketStatus.NEW;
        }
        if (priority == null) {
            priority = SupportTicketPriority.MEDIUM;
        }
        if (category == null) {
            category = SupportTicketCategory.OTHER;
        }
        if (lastStatusChangedAt == null) {
            lastStatusChangedAt = OffsetDateTime.now();
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
