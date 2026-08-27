package com.lifebalance.identity.model;

import com.lifebalance.identity.model.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", schema = "identity")
@SQLDelete(sql = "UPDATE identity.users SET status = 'DELETED', deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Email
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String email;

    @Size(max = 100)
    @Column(length = 100)
    private String username;

    @Size(max = 255)
    @Column(name = "display_name")
    private String displayName;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @Size(max = 50)
    @Column(length = 50)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private OffsetDateTime registeredAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "lock_reason", columnDefinition = "TEXT")
    private String lockReason;

    @Column(name = "locked_at")
    private OffsetDateTime lockedAt;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "locked_by_keycloak_id")
    private String lockedByKeycloakId;

    @Column(name = "token_valid_after")
    private OffsetDateTime tokenValidAfter;

    @Column(name = "keycloak_id")
    private String keycloakId;

    public void setEmail(String email) {
        this.email = normalize(email);
    }

    public void setUsername(String username) {
        this.username = normalize(username);
    }

    public void setPhone(String phone) {
        this.phone = trimToNull(phone);
    }

    public void setGender(String gender) {
        this.gender = trimToNull(gender);
    }

    @PrePersist
    @PreUpdate
    void applyDefaults() {
        email = normalize(email);
        username = normalize(username);
        phone = trimToNull(phone);
        gender = trimToNull(gender);
        if (status == null) {
            status = AccountStatus.ACTIVE;
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public void setKeycloakId(String userId) {
        this.keycloakId = userId;
    }

}
