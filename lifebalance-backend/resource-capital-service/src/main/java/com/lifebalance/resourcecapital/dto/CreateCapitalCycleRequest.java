package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capitalcycle.CapitalCycleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateCapitalCycleRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    private CapitalCycleType type;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

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
}
