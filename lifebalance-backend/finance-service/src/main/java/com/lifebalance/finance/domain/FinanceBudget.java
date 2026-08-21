package com.lifebalance.finance.domain;

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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "finance_budgets", schema = "finance")
public class FinanceBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FinanceCategory category;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "amount_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal amountLimit;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "alert_threshold_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal alertThresholdPercent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BudgetStatus status;

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

    protected FinanceBudget() {
    }

    public static FinanceBudget create(
            UUID ownerId,
            UUID actorId,
            FinanceCategory category,
            String name,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal amountLimit,
            String currencyCode,
            BigDecimal alertThresholdPercent
    ) {
        FinanceBudget budget = new FinanceBudget();
        budget.ownerId = ownerId;
        budget.createdBy = actorId;
        budget.updatedBy = actorId;
        budget.category = category;
        budget.name = name;
        budget.periodStart = periodStart;
        budget.periodEnd = periodEnd;
        budget.amountLimit = amountLimit;
        budget.currencyCode = currencyCode;
        budget.alertThresholdPercent = alertThresholdPercent;
        budget.status = BudgetStatus.ACTIVE;
        return budget;
    }

    public void updateDetails(
            UUID actorId,
            FinanceCategory category,
            String name,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal amountLimit,
            String currencyCode,
            BigDecimal alertThresholdPercent
    ) {
        updatedBy = actorId;
        this.category = category;
        this.name = name;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.amountLimit = amountLimit;
        this.currencyCode = currencyCode;
        this.alertThresholdPercent = alertThresholdPercent;
    }

    public void archive(UUID actorId) {
        updatedBy = actorId;
        status = BudgetStatus.ARCHIVED;
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

    public FinanceCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getAlertThresholdPercent() {
        return alertThresholdPercent;
    }

    public BudgetStatus getStatus() {
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
