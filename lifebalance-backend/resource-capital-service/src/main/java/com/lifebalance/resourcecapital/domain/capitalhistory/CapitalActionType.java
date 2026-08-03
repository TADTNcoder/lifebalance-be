package com.lifebalance.resourcecapital.domain.capitalhistory;

public enum CapitalActionType {
    CYCLE_CREATED,
    CYCLE_UPDATED,
    CYCLE_ACTIVATED,
    CYCLE_CLOSED,
    CYCLE_REOPENED,

    CAPITAL_SET,
    ADJUSTMENT_INCREASE,
    ADJUSTMENT_DECREASE,

    ALLOCATE,
    REALLOCATE,
    RELEASE,

    OVER_ALLOCATION_APPROVED,
    TRANSFER_REMAINING
}
