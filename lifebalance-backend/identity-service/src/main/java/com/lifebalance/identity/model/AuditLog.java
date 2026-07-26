package com.lifebalance.identity.model;

import java.util.UUID;

import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditEntityName;
import com.lifebalance.identity.model.enums.AuditStatus;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "audit_logs",
        schema = "identity",
        indexes = {
                @Index(name = "idx_identity_audit_logs_entity", columnList = "entity_name, entity_id, created_at"),
                @Index(name = "idx_identity_audit_logs_actor", columnList = "actor_id, created_at"),
                @Index(name = "idx_identity_audit_logs_action_created_at", columnList = "action, created_at"),
                @Index(name = "idx_identity_audit_logs_status_created_at", columnList = "status, created_at")
        }
)
public class AuditLog extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_name", nullable = false, length = 50)
    private AuditEntityName entityName;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_keycloak_id")
    private String actorKeycloakId;

    @Column(name = "actor_username", length = 100)
    private String actorUsername;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "keycloak_id")
    private String keycloakId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditStatus status;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String details;

}
