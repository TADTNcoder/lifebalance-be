package com.lifebalance.resourcecapital.domain.timecapital;

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

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "time_capitals",
        schema = "resourcecapital",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_time_capitals_cycle",
                        columnNames = "capital_cycle_id"
                )
        }
)
public class TimeCapital {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "capital_cycle_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_time_capitals_cycle")
    )
    private CapitalCycle capitalCycle;

    @Column(name = "planned_minutes", nullable = false)
    private long plannedMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected TimeCapital() {
    }

    private TimeCapital(CapitalCycle capitalCycle, long plannedMinutes) {
        validateCapitalCycle(capitalCycle);
        validatePlannedMinutes(plannedMinutes);

        capitalCycle.ensureTimeCapitalCanBeInitialized();

        this.capitalCycle = capitalCycle;
        this.plannedMinutes = plannedMinutes;
    }

    public static TimeCapital create(CapitalCycle capitalCycle, long plannedMinutes) {
        return new TimeCapital(capitalCycle, plannedMinutes);
    }

    public boolean hasCapital() {
        return plannedMinutes > 0;
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

    private static void validatePlannedMinutes(long plannedMinutes) {
        if (plannedMinutes < 0) {
            throw new IllegalArgumentException("Planned minutes must be greater than or equal to zero.");
        }
    }

    public UUID getId() {
        return id;
    }

    public CapitalCycle getCapitalCycle() {
        return capitalCycle;
    }

    public long getPlannedMinutes() {
        return plannedMinutes;
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
        if (!(other instanceof TimeCapital that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
