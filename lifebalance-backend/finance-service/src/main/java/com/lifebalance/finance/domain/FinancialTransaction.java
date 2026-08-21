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
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "financial_transactions", schema = "finance")
public class FinancialTransaction {

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
    private FinanceTransactionStatus status;

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

    @Column(name = "transaction_date", nullable = false)
    private OffsetDateTime transactionDate;

    @Column(length = 1000)
    private String description;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "capital_cycle_id")
    private UUID capitalCycleId;

    @Column(name = "capital_allocation_id")
    private UUID capitalAllocationId;

    @Column(name = "voided_at")
    private OffsetDateTime voidedAt;

    @Column(name = "void_reason", length = 1000)
    private String voidReason;

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

    protected FinancialTransaction() {
    }

    public static FinancialTransaction post(
            UUID ownerId,
            UUID actorId,
            FinanceTransactionType transactionType,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            FinanceCategory category,
            BigDecimal amount,
            String currencyCode,
            OffsetDateTime transactionDate,
            String description,
            UUID taskId,
            UUID capitalCycleId,
            UUID capitalAllocationId
    ) {
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.ownerId = ownerId;
        transaction.createdBy = actorId;
        transaction.updatedBy = actorId;
        transaction.transactionType = transactionType;
        transaction.status = FinanceTransactionStatus.POSTED;
        transaction.sourceAccount = sourceAccount;
        transaction.destinationAccount = destinationAccount;
        transaction.category = category;
        transaction.amount = amount;
        transaction.currencyCode = currencyCode;
        transaction.transactionDate = transactionDate;
        transaction.description = description;
        transaction.taskId = taskId;
        transaction.capitalCycleId = capitalCycleId;
        transaction.capitalAllocationId = capitalAllocationId;
        return transaction;
    }

    public void replaceWith(
            UUID actorId,
            FinanceTransactionType transactionType,
            FinanceAccount sourceAccount,
            FinanceAccount destinationAccount,
            FinanceCategory category,
            BigDecimal amount,
            String currencyCode,
            OffsetDateTime transactionDate,
            String description,
            UUID taskId,
            UUID capitalCycleId,
            UUID capitalAllocationId
    ) {
        updatedBy = actorId;
        this.transactionType = transactionType;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.category = category;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.transactionDate = transactionDate;
        this.description = description;
        this.taskId = taskId;
        this.capitalCycleId = capitalCycleId;
        this.capitalAllocationId = capitalAllocationId;
    }

    public void voidTransaction(UUID actorId, String reason) {
        updatedBy = actorId;
        status = FinanceTransactionStatus.VOIDED;
        voidedAt = OffsetDateTime.now();
        voidReason = reason;
    }

    public boolean isPosted() {
        return status == FinanceTransactionStatus.POSTED;
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

    public FinanceTransactionStatus getStatus() {
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

    public OffsetDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getDescription() {
        return description;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getCapitalCycleId() {
        return capitalCycleId;
    }

    public UUID getCapitalAllocationId() {
        return capitalAllocationId;
    }

    public OffsetDateTime getVoidedAt() {
        return voidedAt;
    }

    public String getVoidReason() {
        return voidReason;
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
