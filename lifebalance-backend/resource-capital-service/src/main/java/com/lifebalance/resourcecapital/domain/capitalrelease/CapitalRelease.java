package com.lifebalance.resourcecapital.domain.capitalrelease;

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
        name = "capital_releases",
        schema = "resourcecapital",
        indexes = {
                @Index(name = "idx_capital_releases_allocation_released_at", columnList = "allocation_id,released_at")
        }
)
public class CapitalRelease {

    private static final int AMOUNT_INTEGER_DIGITS = 15;
    private static final int REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "allocation_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capital_releases_allocation")
    )
    private CapitalAllocation allocation;

    @Column(name = "released_amount", nullable = false, precision = 19, scale = CapitalAllocation.AMOUNT_SCALE)
    private BigDecimal releasedAmount;

    @Size(max = REASON_MAX_LENGTH)
    @Column(name = "reason", length = REASON_MAX_LENGTH)
    private String reason;

    @CreatedDate
    @Column(name = "released_at", nullable = false, updatable = false)
    private Instant releasedAt;

    private CapitalRelease(CapitalAllocation allocation, BigDecimal releasedAmount, String reason) {
        this.allocation = requireAllocation(allocation);
        this.releasedAmount = normalizePositiveAmount(releasedAmount);
        this.reason = optionalText(reason, "reason", REASON_MAX_LENGTH);
    }

    public static CapitalRelease record(CapitalAllocation allocation, BigDecimal releasedAmount, String reason) {
        return new CapitalRelease(allocation, releasedAmount, reason);
    }

    @PrePersist
    void onCreate() {
        if (releasedAt == null) {
            releasedAt = Instant.now();
        }
    }

    public UUID getAllocationId() {
        return allocation == null ? null : allocation.getId();
    }

    private static CapitalAllocation requireAllocation(CapitalAllocation allocation) {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation is required.");
        }
        return allocation;
    }

    private static BigDecimal normalizePositiveAmount(BigDecimal amount) {
        if (amount == null) {
            throw InvalidAdjustmentAmountException.invalidMoney("released amount is required");
        }
        BigDecimal normalizedAmount;
        try {
            normalizedAmount = amount.setScale(CapitalAllocation.AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw InvalidAdjustmentAmountException.invalidMoney(
                    "released amount scale must not exceed " + CapitalAllocation.AMOUNT_SCALE
            );
        }
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw InvalidAdjustmentAmountException.invalidMoney("released amount must be greater than zero");
        }
        validateColumnPrecision(normalizedAmount, "released amount");
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
