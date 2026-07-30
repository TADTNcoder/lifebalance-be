package com.lifebalance.resourcecapital.domain.capitalcycle;

import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCyclePeriodException;
import com.lifebalance.resourcecapital.domain.capitalcycle.exception.InvalidCapitalCycleStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "capital_cycles",
        indexes = {
                @Index(name = "idx_capital_cycle_owner_status", columnList = "owner_id,status"),
                @Index(name = "idx_capital_cycle_owner_period", columnList = "owner_id,start_date,end_date")
        }
)
public class CapitalCycle {

    private static final int NAME_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;
    private static final int REASON_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Size(max = DESCRIPTION_MAX_LENGTH)
    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cycle_type", nullable = false, length = 32)
    private CapitalCycleType cycleType;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CapitalCycleStatus status;

    @Column(name = "over_allocation_allowed", nullable = false)
    private boolean overAllocationAllowed;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "reopened_at")
    private Instant reopenedAt;

    @Size(max = REASON_MAX_LENGTH)
    @Column(name = "close_reason", length = REASON_MAX_LENGTH)
    private String closeReason;

    @Size(max = REASON_MAX_LENGTH)
    @Column(name = "reopen_reason", length = REASON_MAX_LENGTH)
    private String reopenReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected CapitalCycle() {
    }

    private CapitalCycle(
            UUID ownerId,
            String name,
            String description,
            CapitalCycleType cycleType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        setCoreInformation(ownerId, name, description, cycleType, startDate, endDate);
        this.status = CapitalCycleStatus.DRAFT;
        this.overAllocationAllowed = false;
    }

    /**
     * Creates a new capital cycle in DRAFT state after validating owner, period, and cycle type rules.
     */
    public static CapitalCycle create(
            UUID ownerId,
            String name,
            String description,
            CapitalCycleType cycleType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new CapitalCycle(ownerId, name, description, cycleType, startDate, endDate);
    }

    /**
     * Updates editable information while the cycle is still mutable.
     */
    public void updateInformation(
            String name,
            String description,
            CapitalCycleType cycleType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        ensureStatusAllows("update information", CapitalCycleStatus.DRAFT, CapitalCycleStatus.REOPENED);
        setCoreInformation(ownerId, name, description, cycleType, startDate, endDate);
    }

    /**
     * Activates a DRAFT or REOPENED cycle. The one-active-cycle rule belongs to the application service.
     */
    public void activate(Instant activatedAt) {
        ensureStatusAllows("activate", CapitalCycleStatus.DRAFT, CapitalCycleStatus.REOPENED);
        this.activatedAt = requireTimestamp(activatedAt, "activatedAt");
        this.status = CapitalCycleStatus.ACTIVE;
    }

    /**
     * Closes an ACTIVE cycle with an explicit business reason.
     */
    public void close(String reason, Instant closedAt) {
        ensureStatusAllows("close", CapitalCycleStatus.ACTIVE);
        this.closeReason = requireReason(reason, "close reason");
        this.closedAt = requireTimestamp(closedAt, "closedAt");
        this.status = CapitalCycleStatus.CLOSED;
    }

    /**
     * Reopens a CLOSED cycle with an explicit business reason.
     */
    public void reopen(String reason, Instant reopenedAt) {
        ensureStatusAllows("reopen", CapitalCycleStatus.CLOSED);
        this.reopenReason = requireReason(reason, "reopen reason");
        this.reopenedAt = requireTimestamp(reopenedAt, "reopenedAt");
        this.status = CapitalCycleStatus.REOPENED;
    }

    /**
     * Allows allocation flows to allocate more than the planned capital for this cycle.
     */
    public void allowOverAllocation() {
        this.overAllocationAllowed = true;
    }

    /**
     * Requires allocation flows to stay within planned capital for this cycle.
     */
    public void disallowOverAllocation() {
        this.overAllocationAllowed = false;
    }

    public boolean contains(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(LocalDate otherStartDate, LocalDate otherEndDate) {
        validateDateRange(otherStartDate, otherEndDate, "overlap check period");
        return !otherStartDate.isAfter(endDate) && !otherEndDate.isBefore(startDate);
    }

    public boolean belongsTo(UUID ownerId) {
        return Objects.equals(this.ownerId, ownerId);
    }

    public boolean isDraft() {
        return status == CapitalCycleStatus.DRAFT;
    }

    public boolean isActive() {
        return status == CapitalCycleStatus.ACTIVE;
    }

    public boolean isClosed() {
        return status == CapitalCycleStatus.CLOSED;
    }

    public boolean isReopened() {
        return status == CapitalCycleStatus.REOPENED;
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
        if (status == null) {
            status = CapitalCycleStatus.DRAFT;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    private void setCoreInformation(
            UUID ownerId,
            String name,
            String description,
            CapitalCycleType cycleType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        UUID validatedOwnerId = requireOwner(ownerId);
        String validatedName = requireText(name, "name", NAME_MAX_LENGTH);
        String validatedDescription = optionalText(description, "description", DESCRIPTION_MAX_LENGTH);
        CapitalCycleType validatedCycleType = requireCycleType(cycleType);
        validatePeriod(validatedCycleType, startDate, endDate);
        this.ownerId = validatedOwnerId;
        this.name = validatedName;
        this.description = validatedDescription;
        this.cycleType = validatedCycleType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private void validatePeriod(CapitalCycleType cycleType, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate, "capital cycle period");
        switch (cycleType) {
            case DAILY -> validateDaily(startDate, endDate);
            case WEEKLY -> validateWeekly(startDate, endDate);
            case MONTHLY -> validateMonthly(startDate, endDate);
        }
    }

    private void validateDaily(LocalDate startDate, LocalDate endDate) {
        if (!startDate.equals(endDate)) {
            throw new InvalidCapitalCyclePeriodException(
                    "Capital cycle " + cycleRef() + " DAILY period must start and end on the same date."
            );
        }
    }

    private void validateWeekly(LocalDate startDate, LocalDate endDate) {
        if (ChronoUnit.DAYS.between(startDate, endDate) != 6) {
            throw new InvalidCapitalCyclePeriodException(
                    "Capital cycle " + cycleRef() + " WEEKLY period must cover exactly 7 days."
            );
        }
    }

    private void validateMonthly(LocalDate startDate, LocalDate endDate) {
        boolean startsAtFirstDay = startDate.getDayOfMonth() == 1;
        boolean endsAtLastDay = endDate.equals(startDate.withDayOfMonth(startDate.lengthOfMonth()));
        if (!startsAtFirstDay) {
            throw new InvalidCapitalCyclePeriodException(
                    "Capital cycle " + cycleRef() + " MONTHLY period must start on the first day of the month."
            );
        }
        if (!endsAtLastDay) {
            throw new InvalidCapitalCyclePeriodException(
                    "Capital cycle " + cycleRef() + " MONTHLY period must end on the last day of the same month."
            );
        }
    }

    private void ensureStatusAllows(String action, CapitalCycleStatus... allowedStatuses) {
        for (CapitalCycleStatus allowedStatus : allowedStatuses) {
            if (status == allowedStatus) {
                return;
            }
        }
        throw new InvalidCapitalCycleStateException(
                id,
                status,
                action,
                "allowed statuses are " + java.util.Arrays.toString(allowedStatuses)
        );
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate, String fieldName) {
        if (startDate == null) {
            throw new InvalidCapitalCyclePeriodException("Capital cycle " + cycleRef() + " " + fieldName + " startDate is required.");
        }
        if (endDate == null) {
            throw new InvalidCapitalCyclePeriodException("Capital cycle " + cycleRef() + " " + fieldName + " endDate is required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidCapitalCyclePeriodException("Capital cycle " + cycleRef() + " startDate must not be after endDate.");
        }
    }

    private UUID requireOwner(UUID ownerId) {
        if (ownerId == null) {
            throw new InvalidCapitalCyclePeriodException("Capital cycle " + cycleRef() + " ownerId is required.");
        }
        return ownerId;
    }

    private CapitalCycleType requireCycleType(CapitalCycleType cycleType) {
        if (cycleType == null) {
            throw new InvalidCapitalCyclePeriodException("Capital cycle " + cycleRef() + " cycleType is required.");
        }
        return cycleType;
    }

    private String requireText(String value, String fieldName, int maxLength) {
        String normalized = optionalText(value, fieldName, maxLength);
        if (normalized == null) {
            throw new InvalidCapitalCyclePeriodException("Capital cycle " + cycleRef() + " " + fieldName + " is required.");
        }
        return normalized;
    }

    private String optionalText(String value, String fieldName, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new InvalidCapitalCyclePeriodException(
                    "Capital cycle " + cycleRef() + " " + fieldName + " must not exceed " + maxLength + " characters."
            );
        }
        return normalized;
    }

    private String requireReason(String reason, String fieldName) {
        return requireText(reason, fieldName, REASON_MAX_LENGTH);
    }

    private Instant requireTimestamp(Instant timestamp, String fieldName) {
        if (timestamp == null) {
            throw new InvalidCapitalCycleStateException(id, status, fieldName, fieldName + " is required.");
        }
        return timestamp;
    }

    private String cycleRef() {
        return id == null ? "<new>" : id.toString();
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

    public String getDescription() {
        return description;
    }

    public CapitalCycleType getCycleType() {
        return cycleType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public CapitalCycleStatus getStatus() {
        return status;
    }

    public boolean isOverAllocationAllowed() {
        return overAllocationAllowed;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getReopenedAt() {
        return reopenedAt;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public String getReopenReason() {
        return reopenReason;
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
        if (!(other instanceof CapitalCycle that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
