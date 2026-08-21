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
@Table(name = "recurring_transaction_rules", schema = "finance")
public class RecurringTransactionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 16)
    private FinanceTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecurringTransactionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private FinanceAccount sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private FinanceAccount destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FinanceCategory category;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecurrenceFrequency frequency;

    @Column(name = "interval_count", nullable = false)
    private int intervalCount;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(length = 1000)
    private String description;

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

    protected RecurringTransactionRule() {
    }

    public static RecurringTransactionRule create(
            UUID ownerId,
            UUID actorId,
            FinanceTransactionType transactionType,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            FinanceCategory category,
            BigDecimal amount,
            String currencyCode,
            RecurrenceFrequency frequency,
            int intervalCount,
            LocalDate startsOn,
            LocalDate nextRunDate,
            LocalDate endsOn,
            String description
    ) {
        RecurringTransactionRule rule = new RecurringTransactionRule();
        rule.ownerId = ownerId;
        rule.createdBy = actorId;
        rule.updatedBy = actorId;
        rule.transactionType = transactionType;
        rule.sourceAccount = sourceAccount;
        rule.destinationAccount = destinationAccount;
        rule.category = category;
        rule.amount = amount;
        rule.currencyCode = currencyCode;
        rule.frequency = frequency;
        rule.intervalCount = intervalCount;
        rule.startsOn = startsOn;
        rule.nextRunDate = nextRunDate;
        rule.endsOn = endsOn;
        rule.description = description;
        rule.status = RecurringTransactionStatus.ACTIVE;
        return rule;
    }

    public void updateDetails(
            UUID actorId,
            FinanceTransactionType transactionType,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            FinanceCategory category,
            BigDecimal amount,
            String currencyCode,
            RecurrenceFrequency frequency,
            int intervalCount,
            LocalDate startsOn,
            LocalDate nextRunDate,
            LocalDate endsOn,
            String description
    ) {
        updatedBy = actorId;
        this.transactionType = transactionType;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.category = category;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.frequency = frequency;
        this.intervalCount = intervalCount;
        this.startsOn = startsOn;
        this.nextRunDate = nextRunDate;
        this.endsOn = endsOn;
        this.description = description;
    }

    public void pause(UUID actorId) {
        updatedBy = actorId;
        status = RecurringTransactionStatus.PAUSED;
    }

    public void resume(UUID actorId) {
        updatedBy = actorId;
        status = RecurringTransactionStatus.ACTIVE;
    }

    public void end(UUID actorId) {
        updatedBy = actorId;
        status = RecurringTransactionStatus.ENDED;
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

    public FinanceTransactionType getTransactionType() {
        return transactionType;
    }

    public RecurringTransactionStatus getStatus() {
        return status;
    }

    public FinanceAccount getSourceAccount() {
        return sourceAccount;
    }

    public FinanceAccount getDestinationAccount() {
        return destinationAccount;
    }

    public FinanceCategory getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public RecurrenceFrequency getFrequency() {
        return frequency;
    }

    public int getIntervalCount() {
        return intervalCount;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public LocalDate getNextRunDate() {
        return nextRunDate;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public String getDescription() {
        return description;
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
