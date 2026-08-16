package com.lifebalance.resourcecapital.domain.capitaladjustment;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Immutable
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "capital_adjustments",
        schema = "resourcecapital",
        indexes = {
                @Index(name = "idx_capital_adjustments_cycle_created_at", columnList = "capital_cycle_id,created_at"),
                @Index(name = "idx_capital_adjustments_cycle_type_created_at", columnList = "capital_cycle_id,capital_type,created_at")
        }
)
public class CapitalAdjustment {

    private static final int AMOUNT_SCALE = 4;
    private static final int REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capital_cycle_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_capital_adjustments_cycle")
    )
    private CapitalCycle capitalCycle;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "capital_type", nullable = false, updatable = false, length = 32)
    private CapitalType capitalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, updatable = false, length = 32)
    private AdjustmentType adjustmentType;

    @Column(name = "amount_delta", nullable = false, updatable = false, precision = 19, scale = AMOUNT_SCALE)
    private BigDecimal amountDelta;

    @Column(name = "previous_amount", nullable = false, updatable = false, precision = 19, scale = AMOUNT_SCALE)
    private BigDecimal previousAmount;

    @Column(name = "new_amount", nullable = false, updatable = false, precision = 19, scale = AMOUNT_SCALE)
    private BigDecimal newAmount;

    @Column(name = "reason", updatable = false, length = REASON_MAX_LENGTH)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static CapitalAdjustment record(
            CapitalCycle capitalCycle,
            UUID userId,
            CapitalKind capitalType,
            CapitalAdjustmentType adjustmentType,
            BigDecimal previousAmount,
            BigDecimal newAmount,
            String reason
    ) {
        CapitalAdjustment adjustment = new CapitalAdjustment();
        adjustment.capitalCycle = Objects.requireNonNull(capitalCycle, "Capital cycle is required.");
        adjustment.userId = Objects.requireNonNull(userId, "Adjustment owner id is required.");
        adjustment.capitalType = Objects.requireNonNull(CapitalType.from(capitalType), "Capital type is required.");
        adjustment.adjustmentType = Objects.requireNonNull(
                AdjustmentType.from(adjustmentType),
                "Adjustment type is required."
        );
        adjustment.previousAmount = normalize(previousAmount, "Previous amount is required.");
        adjustment.newAmount = normalize(newAmount, "New amount is required.");
        adjustment.amountDelta = adjustment.newAmount.subtract(adjustment.previousAmount)
                .setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        adjustment.reason = normalizeReason(reason);
        return adjustment;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public CapitalCycle getCapitalCycle() {
        return capitalCycle;
    }

    public UUID getUserId() {
        return userId;
    }

    public CapitalKind getCapitalType() {
        return capitalType == null ? null : capitalType.toCapitalKind();
    }

    public CapitalAdjustmentType getAdjustmentType() {
        return adjustmentType == null ? null : adjustmentType.toCapitalAdjustmentType();
    }

    public BigDecimal getAmount() {
        return amountDelta == null ? null : amountDelta.abs();
    }

    public BigDecimal getAmountDelta() {
        return amountDelta;
    }

    public BigDecimal getPreviousAmount() {
        return previousAmount;
    }

    public BigDecimal getNewAmount() {
        return newAmount;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public CapitalType getCapitalTypeValue() {
        return capitalType;
    }

    public AdjustmentType getAdjustmentTypeValue() {
        return adjustmentType;
    }

    public void setCapitalType(CapitalKind capitalType) {
        this.capitalType = CapitalType.from(capitalType);
    }

    public void setAdjustmentType(CapitalAdjustmentType adjustmentType) {
        this.adjustmentType = AdjustmentType.from(adjustmentType);
    }

    private static BigDecimal normalize(BigDecimal amount, String message) {
        if (amount == null) {
            throw new IllegalArgumentException(message);
        }
        return amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }

    private static String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }

        String normalized = reason.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > REASON_MAX_LENGTH) {
            throw new IllegalArgumentException("Adjustment reason must not exceed " + REASON_MAX_LENGTH + " characters.");
        }
        return normalized;
    }
}
