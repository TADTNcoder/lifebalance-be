package com.lifebalance.resourcecapital.domain.capitalreallocation;

import com.lifebalance.resourcecapital.domain.capital.exception.InvalidAdjustmentAmountException;
import com.lifebalance.resourcecapital.domain.capitalallocation.CapitalAllocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "capital_reallocations",
        schema = "resourcecapital",
        indexes = {
                @Index(name = "idx_capital_reallocations_from", columnList = "from_allocation_id,created_at"),
                @Index(name = "idx_capital_reallocations_to", columnList = "to_allocation_id,created_at")
        }
)
public class CapitalReallocation {

    private static final int AMOUNT_INTEGER_DIGITS = 15;
    private static final int REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "from_allocation_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capital_reallocations_from_allocation")
    )
    private CapitalAllocation fromAllocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "to_allocation_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capital_reallocations_to_allocation")
    )
    private CapitalAllocation toAllocation;

    @Column(name = "amount", nullable = false, precision = 19, scale = CapitalAllocation.AMOUNT_SCALE)
    private BigDecimal amount;

    @Size(max = REASON_MAX_LENGTH)
    @Column(name = "reason", length = REASON_MAX_LENGTH)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private CapitalReallocation(
            CapitalAllocation fromAllocation,
            CapitalAllocation toAllocation,
            BigDecimal amount,
            String reason
    ) {
        this.fromAllocation = requireAllocation(fromAllocation, "Source allocation is required.");
        this.toAllocation = requireAllocation(toAllocation, "Destination allocation is required.");
        if (this.fromAllocation == this.toAllocation) {
            throw new IllegalArgumentException("Source and destination allocations must be different.");
        }
        this.amount = normalizePositiveAmount(amount);
        this.reason = optionalText(reason, "reason", REASON_MAX_LENGTH);
    }

    public static CapitalReallocation record(
            CapitalAllocation fromAllocation,
            CapitalAllocation toAllocation,
            BigDecimal amount,
            String reason
    ) {
        return new CapitalReallocation(fromAllocation, toAllocation, amount, reason);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getFromAllocationId() {
        return fromAllocation == null ? null : fromAllocation.getId();
    }

    public UUID getToAllocationId() {
        return toAllocation == null ? null : toAllocation.getId();
    }

    private static CapitalAllocation requireAllocation(CapitalAllocation allocation, String message) {
        if (allocation == null) {
            throw new IllegalArgumentException(message);
        }
        return allocation;
    }

    private static BigDecimal normalizePositiveAmount(BigDecimal amount) {
        if (amount == null) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount is required");
        }
        BigDecimal normalizedAmount;
        try {
            normalizedAmount = amount.setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "amount scale must not exceed " + CapitalAllocation.AMOUNT_SCALE
            );
        }
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidAdjustmentAmountException.invalidMoney("amount must be greater than zero");
        }
        validateColumnPrecision(normalizedAmount, "reallocation amount");
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

    private static String optionalText(String value, String fieldName, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must not exceed " + maxLength + " characters.");
        }
        return normalized;
    }
}
