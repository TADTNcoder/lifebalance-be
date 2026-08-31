package com.lifebalance.finance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "finance_monthly_jar_settlements", schema = "finance")
public class FinanceMonthlyJarSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_pool_account_id", nullable = false)
    private FinanceAccount mainPoolAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jar_account_id", nullable = false)
    private FinanceAccount jarAccount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_transaction_id")
    private FinancialTransaction settlementTransaction;

    @Column(name = "allocated_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal allocatedAmount;

    @Column(name = "actual_expense_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal actualExpenseAmount;

    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal closingBalance;

    @Column(name = "transferred_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal transferredAmount;

    @Column(name = "variance_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal varianceAmount;

    @Column(name = "settled_at", nullable = false)
    private OffsetDateTime settledAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected FinanceMonthlyJarSettlement() {
    }

    public static FinanceMonthlyJarSettlement create(
            UUID ownerId,
            LocalDate periodStart,
            String currencyCode,
            FinanceAccount mainPoolAccount,
            FinanceAccount jarAccount,
            FinancialTransaction settlementTransaction,
            BigDecimal allocatedAmount,
            BigDecimal actualExpenseAmount,
            BigDecimal closingBalance,
            BigDecimal transferredAmount,
            BigDecimal varianceAmount,
            OffsetDateTime settledAt
    ) {
        FinanceMonthlyJarSettlement settlement = new FinanceMonthlyJarSettlement();
        settlement.ownerId = ownerId;
        settlement.periodStart = periodStart;
        settlement.currencyCode = currencyCode;
        settlement.mainPoolAccount = mainPoolAccount;
        settlement.jarAccount = jarAccount;
        settlement.settlementTransaction = settlementTransaction;
        settlement.allocatedAmount = allocatedAmount;
        settlement.actualExpenseAmount = actualExpenseAmount;
        settlement.closingBalance = closingBalance;
        settlement.transferredAmount = transferredAmount;
        settlement.varianceAmount = varianceAmount;
        settlement.settledAt = settledAt;
        return settlement;
    }

    @PrePersist
    void onCreate() {
        createdAt = createdAt == null ? OffsetDateTime.now() : createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public FinanceAccount getMainPoolAccount() {
        return mainPoolAccount;
    }

    public FinanceAccount getJarAccount() {
        return jarAccount;
    }

    public FinancialTransaction getSettlementTransaction() {
        return settlementTransaction;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public BigDecimal getActualExpenseAmount() {
        return actualExpenseAmount;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public BigDecimal getTransferredAmount() {
        return transferredAmount;
    }

    public BigDecimal getVarianceAmount() {
        return varianceAmount;
    }

    public OffsetDateTime getSettledAt() {
        return settledAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
