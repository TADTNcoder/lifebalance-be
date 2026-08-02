package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleStatus;
import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CapitalCycleResponse {

    private UUID id;
    private String name;
    private String description;
    private CapitalCycleType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private CapitalCycleStatus status;
    private boolean overAllocationAllowed;
    private Instant activatedAt;
    private Instant closedAt;
    private Instant reopenedAt;
    private String closeReason;
    private String reopenReason;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CapitalCycleType getType() {
        return type;
    }

    public void setType(CapitalCycleType type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public CapitalCycleStatus getStatus() {
        return status;
    }

    public void setStatus(CapitalCycleStatus status) {
        this.status = status;
    }

    public boolean isOverAllocationAllowed() {
        return overAllocationAllowed;
    }

    public void setOverAllocationAllowed(boolean overAllocationAllowed) {
        this.overAllocationAllowed = overAllocationAllowed;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(Instant activatedAt) {
        this.activatedAt = activatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public Instant getReopenedAt() {
        return reopenedAt;
    }

    public void setReopenedAt(Instant reopenedAt) {
        this.reopenedAt = reopenedAt;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    public String getReopenReason() {
        return reopenReason;
    }

    public void setReopenReason(String reopenReason) {
        this.reopenReason = reopenReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
