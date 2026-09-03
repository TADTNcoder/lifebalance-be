package com.lifebalance.analytics.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ActualRecordAggregateRepository {

    Long sumActualMinutes(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            LocalDate from,
            LocalDate to
    );

    BigDecimal sumActualCost(
            UUID ownerId,
            UUID taskId,
            UUID capitalCycleId,
            String currencyCode,
            LocalDate from,
            LocalDate to
    );
}
