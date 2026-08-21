package com.lifebalance.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "finance_categories", schema = "finance")
public class FinanceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 16)
    private FinanceCategoryType categoryType;

    @Column(length = 20)
    private String color;

    @Column(length = 50)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FinanceCategoryStatus status;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected FinanceCategory() {
    }

    public static FinanceCategory create(
            UUID ownerId,
            UUID actorId,
            String name,
            FinanceCategoryType categoryType,
            String color,
            String icon
    ) {
        FinanceCategory category = new FinanceCategory();
        category.ownerId = ownerId;
        category.createdBy = actorId;
        category.updatedBy = actorId;
        category.name = name;
        category.categoryType = categoryType;
        category.color = color;
        category.icon = icon;
        category.status = FinanceCategoryStatus.ACTIVE;
        return category;
    }

    public void updateDetails(UUID actorId, String name, String color, String icon) {
        updatedBy = actorId;
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public void archive(UUID actorId) {
        updatedBy = actorId;
        status = FinanceCategoryStatus.ARCHIVED;
    }

    public boolean isActive() {
        return status == FinanceCategoryStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = createdAt == null ? now : createdAt;
        updatedAt = updatedAt == null ? now : updatedAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public FinanceCategoryType getCategoryType() {
        return categoryType;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public FinanceCategoryStatus getStatus() {
        return status;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
