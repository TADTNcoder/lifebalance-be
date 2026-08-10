package com.lifebalance.resourcecapital.domain.capitaladjustment;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capital_cycle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_capital_adjustments_cycle")
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CapitalCycle capitalCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "capital_type", nullable = false, length = 32)
    private CapitalKind capitalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 32)
    private CapitalAdjustmentType adjustmentType;

    @Column(name = "amount", nullable = false, precision = 19, scale = AMOUNT_SCALE)
    private BigDecimal amount;

    @Column(name = "reason", length = REASON_MAX_LENGTH)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
