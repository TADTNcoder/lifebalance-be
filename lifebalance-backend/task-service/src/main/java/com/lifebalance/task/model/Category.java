package com.lifebalance.task.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;
import java.util.UUID;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.lifebalance.task.util.SlugGenerator;

@Entity
@Table(name = "categories", schema = "task")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = """
        UPDATE task.categories
        SET deleted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = ?
        """)
@SQLRestriction("deleted_at IS NULL")
public class Category extends BaseAuditableEntity {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int SLUG_MAX_LENGTH = 100;
    private static final int COLOR_MAX_LENGTH = 20;
    private static final int ICON_MAX_LENGTH = 50;
    private static final String HEX_COLOR_PATTERN = "^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    @Column(nullable = false, unique = true, length = NAME_MAX_LENGTH)
    private String name;

    @Size(max = SLUG_MAX_LENGTH)
    @Column(nullable = false, length = SLUG_MAX_LENGTH)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = COLOR_MAX_LENGTH)
    @Pattern(regexp = HEX_COLOR_PATTERN, message = "Category color must be a valid hex color.")
    @Column(length = COLOR_MAX_LENGTH)
    private String color;

    @Size(max = ICON_MAX_LENGTH)
    @Column(length = ICON_MAX_LENGTH)
    private String icon;

    @NotNull
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = Boolean.FALSE;

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
}
