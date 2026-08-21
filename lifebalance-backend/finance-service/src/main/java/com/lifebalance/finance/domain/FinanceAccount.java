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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "finance_accounts", schema = "finance")
public class FinanceAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 32)
    private FinanceAccountType accountType;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FinanceAccountStatus status;

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

    protected FinanceAccount() {
    }

    public static FinanceAccount create(
            UUID ownerId,
            UUID actorId,
            String name,
            FinanceAccountType accountType,
            String currencyCode,
            BigDecimal openingBalance
    ) {
        FinanceAccount account = new FinanceAccount();
        account.ownerId = ownerId;
        account.createdBy = actorId;
        account.updatedBy = actorId;
        account.name = name;
        account.accountType = accountType;
        account.currencyCode = currencyCode;
        account.openingBalance = openingBalance;
        account.currentBalance = openingBalance;
        account.status = FinanceAccountStatus.ACTIVE;
        return account;
    }

    public void updateDetails(UUID actorId, String name, FinanceAccountType accountType) {
        this.updatedBy = actorId;
        this.name = name;
        this.accountType = accountType;
    }

    public void credit(BigDecimal amount) {
        currentBalance = currentBalance.add(amount);
    }

    public void debit(BigDecimal amount) {
        currentBalance = currentBalance.subtract(amount);
    }

    public void archive(UUID actorId) {
        updatedBy = actorId;
        status = FinanceAccountStatus.ARCHIVED;
    }

    public boolean isActive() {
        return status == FinanceAccountStatus.ACTIVE;
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

    public FinanceAccountType getAccountType() {
        return accountType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public FinanceAccountStatus getStatus() {
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
