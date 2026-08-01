package com.lifebalance.resourcecapital.domain.moneycapital;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "money_capitals",
        schema = "resourcecapital",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_money_capitals_cycle",
                        columnNames = "capital_cycle_id"
                )
        }
)
public class MoneyCapital {

    private static final int AMOUNT_SCALE = 4;
    private static final int CURRENCY_CODE_LENGTH = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capital_cycle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_money_capitals_cycle")
    )
    private CapitalCycle capitalCycle;

    @Column(name = "planned_amount", precision = 19, scale = AMOUNT_SCALE, nullable = false)
    private BigDecimal plannedAmount;

    @Column(name = "currency_code", length = CURRENCY_CODE_LENGTH, nullable = false)
    private String currencyCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected MoneyCapital() {
    }

    private MoneyCapital(CapitalCycle capitalCycle, BigDecimal plannedAmount, String currencyCode) {
        validateCapitalCycle(capitalCycle);
        validateAmount(plannedAmount);
        validateCurrencyCode(currencyCode);

        capitalCycle.ensureMoneyCapitalCanBeInitialized();

        this.capitalCycle = capitalCycle;
        this.plannedAmount = plannedAmount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        this.currencyCode = currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    public static MoneyCapital create(CapitalCycle capitalCycle, BigDecimal plannedAmount, String currencyCode) {
        return new MoneyCapital(capitalCycle, plannedAmount, currencyCode);
    }

    public boolean hasCapital() {
        return plannedAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    private static void validateCapitalCycle(CapitalCycle capitalCycle) {
        if (capitalCycle == null) {
            throw new IllegalArgumentException("Capital cycle must not be null.");
        }
    }

    private static void validateAmount(BigDecimal plannedAmount) {
        if (plannedAmount == null) {
            throw new IllegalArgumentException("Planned amount must not be null.");
        }
        if (plannedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Planned amount must be greater than or equal to zero.");
        }
    }

    private static void validateCurrencyCode(String currencyCode) {
        if (currencyCode == null || !currencyCode.trim().matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Currency code must contain exactly three letters.");
        }
    }

    public UUID getId() {
        return id;
    }

    public CapitalCycle getCapitalCycle() {
        return capitalCycle;
    }

    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MoneyCapital that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
