package com.lifebalance.identity.model;

import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.lifebalance.identity.model.enums.SupportTicketStatus;
import com.lifebalance.identity.model.enums.TicketHistoryAction;

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
        name = "support_ticket_history",
        schema = "identity",
        indexes = {
                @Index(name = "idx_identity_ticket_history_ticket", columnList = "ticket_id, created_at"),
                @Index(name = "idx_identity_ticket_history_actor", columnList = "actor_id, created_at"),
                @Index(name = "idx_identity_ticket_history_action", columnList = "action, created_at")
        }
)
@SQLDelete(sql = "UPDATE identity.support_ticket_history SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SupportTicketHistory extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TicketHistoryAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    private SupportTicketStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 32)
    private SupportTicketStatus newStatus;

    @Column(name = "previous_assignee_id")
    private UUID previousAssigneeId;

    @Column(name = "new_assignee_id")
    private UUID newAssigneeId;

    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @PrePersist
    void applyDefaults() {
        commentText = trim(commentText);
        reason = trim(reason);
        oldValue = trim(oldValue);
        newValue = trim(newValue);
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
