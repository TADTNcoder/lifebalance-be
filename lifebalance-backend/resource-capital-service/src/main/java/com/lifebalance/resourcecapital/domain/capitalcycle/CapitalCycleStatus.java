package com.lifebalance.resourcecapital.domain.capitalcycle;

public enum CapitalCycleStatus {
    DRAFT,
    ACTIVE,
    CLOSED,
    REOPENED;

    public boolean canTransitionTo(CapitalCycleStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        return switch (this) {
            case DRAFT -> targetStatus == ACTIVE;
            case ACTIVE -> targetStatus == CLOSED;
            case CLOSED -> targetStatus == REOPENED;
            case REOPENED -> targetStatus == ACTIVE || targetStatus == CLOSED;
        };
    }
}
