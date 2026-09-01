package com.lifebalance.analytics.service.impl;

import java.time.LocalDate;
import java.util.UUID;

/** Immutable task scope used when an actual record mutation triggers evaluation. */
record AutomaticEvaluationTarget(
        UUID taskId,
        UUID capitalCycleId,
        LocalDate actualDate,
        String currencyCode,
        boolean hasTime,
        boolean hasCost,
        boolean active
) {
}
