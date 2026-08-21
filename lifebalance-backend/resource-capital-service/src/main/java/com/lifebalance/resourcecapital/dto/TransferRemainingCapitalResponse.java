package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferRemainingCapitalResponse(
        UUID sourceCycleId,
        UUID targetCycleId,
        CapitalKind capitalType,
        BigDecimal amount,
        BigDecimal sourceBeforeAmount,
        BigDecimal sourceAfterAmount,
        BigDecimal targetBeforeAmount,
        BigDecimal targetAfterAmount,
        String reason,
        UUID sourceHistoryId,
        UUID targetHistoryId,
        Instant transferredAt
) {
}
