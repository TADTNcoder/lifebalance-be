package com.lifebalance.identity.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.lifebalance.identity.model.enums.SystemConfigurationValueType;

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
import java.util.Locale;
import java.util.UUID;
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
        name = "system_configurations",
        schema = "identity",
        indexes = {
                @Index(name = "idx_identity_system_configurations_key", columnList = "config_key"),
                @Index(name = "idx_identity_system_configurations_editable", columnList = "editable, sensitive")
        }
)
@SQLDelete(sql = "UPDATE identity.system_configurations SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SystemConfiguration extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "config_key", nullable = false, unique = true, length = 150)
    private String configKey;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 32)
    private SystemConfigurationValueType valueType;

    @Column(nullable = false)
    private Boolean sensitive;

    @Column(nullable = false)
    private Boolean editable;

    @Column(name = "requires_confirmation", nullable = false)
    private Boolean requiresConfirmation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "last_change_reason", columnDefinition = "TEXT")
    private String lastChangeReason;

    @PrePersist
    @PreUpdate
    void applyDefaults() {
        configKey = normalizeKey(configKey);
        displayName = trim(displayName);
        description = trim(description);
        configValue = trim(configValue);
        lastChangeReason = trim(lastChangeReason);
        if (valueType == null) {
            valueType = SystemConfigurationValueType.STRING;
        }
        if (sensitive == null) {
            sensitive = false;
        }
        if (editable == null) {
            editable = true;
        }
        if (requiresConfirmation == null) {
            requiresConfirmation = false;
        }
    }

    private static String normalizeKey(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
