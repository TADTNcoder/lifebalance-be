package com.lifebalance.task.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.lifebalance.task.util.SlugGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tags", schema = "task")
@SQLDelete(sql = """
        UPDATE task.tags
        SET deleted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """)
@SQLRestriction("deleted_at IS NULL")
public class Tag extends BaseAuditableEntity {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int SLUG_MAX_LENGTH = 100;
    private static final int COLOR_MAX_LENGTH = 20;
    private static final String HEX_COLOR_PATTERN = "^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Size(max = SLUG_MAX_LENGTH)
    @Column(name = "slug", nullable = false, length = SLUG_MAX_LENGTH)
    private String slug;

    @Size(max = COLOR_MAX_LENGTH)
    @Pattern(regexp = HEX_COLOR_PATTERN, message = "Tag color must be a valid hex color.")
    @Column(name = "color", length = COLOR_MAX_LENGTH)
    private String color;

    @NotNull
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = Boolean.FALSE;

    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TaskTag> taskTags = new HashSet<>();

    @PrePersist
    @PreUpdate
    void applyDefaults() {
        if (slug == null || slug.isBlank()) {
            slug = SlugGenerator.from(name);
        } else {
            slug = SlugGenerator.from(slug);
        }
        if (isSystem == null) {
            isSystem = Boolean.FALSE;
        }
    }

    public void setName(String name) {
        this.name = name;
        if (slug == null || slug.isBlank()) {
            slug = SlugGenerator.from(name);
        }
    }

    public void setSlug(String slug) {
        this.slug = SlugGenerator.from(slug);
    }

    public boolean belongsTo(UUID userId) {
        return Objects.equals(this.userId, userId);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
