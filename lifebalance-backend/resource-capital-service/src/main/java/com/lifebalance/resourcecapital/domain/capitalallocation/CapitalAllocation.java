package com.lifebalance.resourcecapital.domain.capitalallocation;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "capital_allocations",
        schema = "resourcecapital",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_capital_allocations_target",
                        columnNames = {"capital_cycle_id", "capital_type", "target_type", "target_id"}
                )
        },
        indexes = {
                @Index(name = "idx_capital_allocations_cycle_kind", columnList = "capital_cycle_id,capital_type"),
                @Index(name = "idx_capital_allocations_target", columnList = "target_type,target_id")
        }
)
public class CapitalAllocation {

    public static final int AMOUNT_SCALE = 4;
    private static final int AMOUNT_INTEGER_DIGITS = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capital_cycle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capital_allocations_cycle")
    )
    private CapitalCycle capitalCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "capital_type", nullable = false, length = 32)
    private CapitalKind capitalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 64)
    private AllocationTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "allocated_amount", precision = 19, scale = AMOUNT_SCALE, nullable = false)
    private BigDecimal allocatedAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected CapitalAllocation() {
    }

    private CapitalAllocation(
            CapitalCycle capitalCycle,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId,
            BigDecimal allocatedAmount
    ) {
        this.capitalCycle = requireCapitalCycle(capitalCycle);
        this.capitalType = requireCapitalType(capitalType);
        this.targetType = requireTargetType(targetType);
        this.targetId = requireTargetId(targetId);
        this.allocatedAmount = normalizePositiveAmount(allocatedAmount);
    }

    public static CapitalAllocation create(
            CapitalCycle capitalCycle,
            CapitalKind capitalType,
            AllocationTargetType targetType,
            UUID targetId,
            BigDecimal allocatedAmount
    ) {
        return new CapitalAllocation(capitalCycle, capitalType, targetType, targetId, allocatedAmount);
    }

    public void increase(BigDecimal amount) {
        BigDecimal normalizedAmount = normalizePositiveAmount(amount);
        BigDecimal nextAmount = allocatedAmount.add(normalizedAmount);
        validateColumnPrecision(nextAmount, "allocated amount after increase");
        allocatedAmount = nextAmount;
    }

    public void decrease(BigDecimal amount) {
        BigDecimal normalizedAmount = normalizePositiveAmount(amount);
        BigDecimal nextAmount = allocatedAmount.subtract(normalizedAmount);
        if (nextAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "cannot decrease allocated amount " + allocatedAmount + " by " + normalizedAmount
            );
        }
        allocatedAmount = nextAmount;
    }

    public boolean isDepleted() {
        return allocatedAmount.compareTo(BigDecimal.ZERO) == 0;
    }

    private static CapitalCycle requireCapitalCycle(CapitalCycle capitalCycle) {
        if (capitalCycle == null) {
            throw new IllegalArgumentException("Capital cycle must not be null.");
        }
        return capitalCycle;
    }

    private static CapitalKind requireCapitalType(CapitalKind capitalType) {
        if (capitalType == null) {
            throw new IllegalArgumentException("Capital type must not be null.");
        }
        return capitalType;
    }

    private static AllocationTargetType requireTargetType(AllocationTargetType targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Allocation target type must not be null.");
        }
        return targetType;
    }

    private static UUID requireTargetId(UUID targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("Allocation target id must not be null.");
        }
        return targetId;
    }

    private static BigDecimal normalizePositiveAmount(BigDecimal amount) {
        if (amount == null) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount is required");
        }
        BigDecimal normalizedAmount;
        try {
            normalizedAmount = amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount scale must not exceed " + AMOUNT_SCALE);
        }
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount must be greater than zero");
        }
        validateColumnPrecision(normalizedAmount, "allocation amount");
        return normalizedAmount;
    }

    private static void validateColumnPrecision(BigDecimal amount, String fieldName) {
        int integerDigits = amount.precision() - amount.scale();
        if (integerDigits > AMOUNT_INTEGER_DIGITS) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    fieldName + " exceeds " + AMOUNT_INTEGER_DIGITS + " integer digits"
            );
        }
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

    public UUID getId() {
        return id;
    }

    public CapitalCycle getCapitalCycle() {
        return capitalCycle;
    }

    public CapitalKind getCapitalType() {
        return capitalType;
    }

    public AllocationTargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
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
        if (!(other instanceof CapitalAllocation that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
