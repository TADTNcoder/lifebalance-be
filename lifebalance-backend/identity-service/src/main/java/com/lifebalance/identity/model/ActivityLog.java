package com.lifebalance.identity.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.lifebalance.identity.model.enums.ActivityCategory;

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
        name = "activity_logs",
        schema = "identity",
        indexes = {
                @Index(name = "idx_identity_activity_logs_actor", columnList = "actor_id, occurred_at"),
                @Index(name = "idx_identity_activity_logs_category", columnList = "category, occurred_at"),
                @Index(name = "idx_identity_activity_logs_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_identity_activity_logs_action", columnList = "action, occurred_at")
        }
)
@SQLDelete(sql = "UPDATE identity.activity_logs SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ActivityLog extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "actor_keycloak_id", length = 255)
    private String actorKeycloakId;

    @Column(name = "actor_username", length = 100)
    private String actorUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ActivityCategory category;

    @Column(nullable = false, length = 120)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 120)
    private String entityId;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @PrePersist
    void applyDefaults() {
        action = trim(action);
        entityType = trim(entityType);
        entityId = trim(entityId);
        summary = trim(summary);
        details = trim(details);
        actorKeycloakId = trim(actorKeycloakId);
        actorUsername = trim(actorUsername);
        if (occurredAt == null) {
            occurredAt = OffsetDateTime.now();
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
