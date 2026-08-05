package com.lifebalance.resourcecapital.domain.capitalhistory;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "capital_histories",
        schema = "resourcecapital",
        indexes = {
                @Index(name = "idx_capital_histories_cycle_created_at", columnList = "capital_cycle_id,created_at"),
                @Index(name = "idx_capital_histories_cycle_kind_created_at", columnList = "capital_cycle_id,capital_type,created_at"),
                @Index(name = "idx_capital_histories_cycle_action_created_at", columnList = "capital_cycle_id,action_type,created_at"),
                @Index(name = "idx_capital_histories_reference", columnList = "reference_type,reference_id,created_at")
        }
)
public class CapitalHistory {

    private static final int AMOUNT_SCALE = 4;
    private static final int REASON_MAX_LENGTH = 1000;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capital_cycle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capital_histories_cycle")
    )
    private CapitalCycle capitalCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "capital_type", length = 32)
    private CapitalKind capitalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 64)
    private CapitalActionType actionType;

    @Column(name = "amount", precision = 19, scale = AMOUNT_SCALE)
    private BigDecimal amount;

    @Column(name = "before_amount", precision = 19, scale = AMOUNT_SCALE)
    private BigDecimal beforeAmount;

    @Column(name = "after_amount", precision = 19, scale = AMOUNT_SCALE)
    private BigDecimal afterAmount;

    @Size(max = REASON_MAX_LENGTH)
    @Column(name = "reason", length = REASON_MAX_LENGTH)
    private String reason;

    @Size(max = DESCRIPTION_MAX_LENGTH)
    @Column(name = "description", length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 64)
    private CapitalReferenceType referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false, length = 32)
    private CapitalActorType actorType;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CapitalHistory() {
    }

    private CapitalHistory(
            CapitalCycle capitalCycle,
            CapitalKind capitalType,
            CapitalActionType actionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            String reason,
            String description,
            CapitalReferenceType referenceType,
            UUID referenceId,
            CapitalActorType actorType,
            UUID actorId,
            Instant createdAt
    ) {
        this.capitalCycle = requireCapitalCycle(capitalCycle);
        this.actionType = requireActionType(actionType);
        validateCapitalType(actionType, capitalType);
        validateAmounts(actionType, amount, beforeAmount, afterAmount);
        validateReference(referenceType, referenceId);
        validateActor(actorType, actorId);

        this.capitalType = capitalType;
        this.amount = normalizeAmount(amount);
        this.beforeAmount = normalizeAmount(beforeAmount);
        this.afterAmount = normalizeAmount(afterAmount);
        this.reason = optionalText(reason, "reason", REASON_MAX_LENGTH);
        this.description = optionalText(description, "description", DESCRIPTION_MAX_LENGTH);
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.actorType = actorType;
        this.actorId = actorId;
        this.createdAt = createdAt;
    }

    public static CapitalHistory record(
            CapitalCycle capitalCycle,
            CapitalKind capitalType,
            CapitalActionType actionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            String reason,
            String description,
            CapitalReferenceType referenceType,
            UUID referenceId,
            CapitalActorType actorType,
            UUID actorId
    ) {
        return recordAt(
                capitalCycle,
                capitalType,
                actionType,
                amount,
                beforeAmount,
                afterAmount,
                reason,
                description,
                referenceType,
                referenceId,
                actorType,
                actorId,
                null
        );
    }

    public static CapitalHistory recordAt(
            CapitalCycle capitalCycle,
            CapitalKind capitalType,
            CapitalActionType actionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount,
            String reason,
            String description,
            CapitalReferenceType referenceType,
            UUID referenceId,
            CapitalActorType actorType,
            UUID actorId,
            Instant createdAt
    ) {
        return new CapitalHistory(
                capitalCycle,
                capitalType,
                actionType,
                amount,
                beforeAmount,
                afterAmount,
                reason,
                description,
                referenceType,
                referenceId,
                actorType,
                actorId,
                createdAt
        );
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    private static CapitalCycle requireCapitalCycle(CapitalCycle capitalCycle) {
        if (capitalCycle == null) {
            throw new IllegalArgumentException("Capital cycle must not be null.");
        }
        return capitalCycle;
    }

    private static CapitalActionType requireActionType(CapitalActionType actionType) {
        if (actionType == null) {
            throw new IllegalArgumentException("Capital action type must not be null.");
        }
        return actionType;
    }

    private static void validateCapitalType(CapitalActionType actionType, CapitalKind capitalType) {
        if (isCycleAction(actionType)) {
            if (capitalType != null) {
                throw new IllegalArgumentException("Capital type must be null for capital cycle actions.");
            }
            return;
        }
        if (capitalType == null) {
            throw new IllegalArgumentException("Capital type is required for capital amount actions.");
        }
    }

    private static void validateAmounts(
            CapitalActionType actionType,
            BigDecimal amount,
            BigDecimal beforeAmount,
            BigDecimal afterAmount
    ) {
        if (isCycleAction(actionType)) {
            if (amount != null || beforeAmount != null || afterAmount != null) {
                throw new IllegalArgumentException("Amounts must be null for capital cycle actions.");
            }
            return;
        }

        requireAmount(amount, "amount");
        requireAmount(beforeAmount, "beforeAmount");
        requireAmount(afterAmount, "afterAmount");

        if (actionType == CapitalActionType.CAPITAL_SET) {
            requireZeroOrPositive(amount, "amount");
        } else {
            requirePositive(amount, "amount");
        }

        if (actionType == CapitalActionType.OVER_ALLOCATION_APPROVED) {
            return;
        }

        requireZeroOrPositive(beforeAmount, "beforeAmount");
        requireZeroOrPositive(afterAmount, "afterAmount");
    }

    private static void validateReference(CapitalReferenceType referenceType, UUID referenceId) {
        if (referenceType == CapitalReferenceType.MANUAL) {
            if (referenceId != null) {
                throw new IllegalArgumentException("Manual capital history reference must not have a reference id.");
            }
            return;
        }
        if ((referenceType == null) != (referenceId == null)) {
            throw new IllegalArgumentException("Reference type and reference id must both be null or both be provided.");
        }
    }

    private static void validateActor(CapitalActorType actorType, UUID actorId) {
        if (actorType == null) {
            throw new IllegalArgumentException("Capital actor type must not be null.");
        }
        if (actorType == CapitalActorType.USER && actorId == null) {
            throw new IllegalArgumentException("Actor id is required for user capital history events.");
        }
    }

    private static boolean isCycleAction(CapitalActionType actionType) {
        return actionType == CapitalActionType.CYCLE_CREATED
                || actionType == CapitalActionType.CYCLE_UPDATED
                || actionType == CapitalActionType.CYCLE_ACTIVATED
                || actionType == CapitalActionType.CYCLE_CLOSED
                || actionType == CapitalActionType.CYCLE_REOPENED;
    }

    private static BigDecimal requireAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new IllegalArgumentException(fieldName + " is required for capital amount actions.");
        }
        return amount;
    }

    private static void requirePositive(BigDecimal amount, String fieldName) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero.");
        }
    }

    private static void requireZeroOrPositive(BigDecimal amount, String fieldName) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to zero.");
        }
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
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

    public UUID getId() {
        return id;
    }

    public CapitalCycle getCapitalCycle() {
        return capitalCycle;
    }

    public CapitalKind getCapitalType() {
        return capitalType;
    }

    public CapitalActionType getActionType() {
        return actionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBeforeAmount() {
        return beforeAmount;
    }

    public BigDecimal getAfterAmount() {
        return afterAmount;
    }

    public String getReason() {
        return reason;
    }

    public String getDescription() {
        return description;
    }

    public CapitalReferenceType getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public CapitalActorType getActorType() {
        return actorType;
    }

    public UUID getActorId() {
        return actorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CapitalHistory that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
