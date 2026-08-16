package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalAdjustmentType;
import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CapitalAdjustmentResponse(
        Long id,
        UUID capitalCycleId,
        CapitalKind capitalType,
        CapitalAdjustmentType adjustmentType,
        CapitalActionType historyActionType,
        BigDecimal amount,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        String reason,
        UUID historyId,
        LocalDateTime createdAt
) {

    public static CapitalAdjustmentResponse from(CapitalAdjustmentResponseDTO response) {
        return new CapitalAdjustmentResponse(
                response.id(),
                response.capitalCycleId(),
                response.capitalType(),
                response.adjustmentType(),
                response.historyActionType(),
                response.amountDelta() == null ? null : response.amountDelta().abs(),
                response.previousAmount(),
                response.newAmount(),
                response.reason(),
                response.historyId(),
                response.createdAt()
        );
    }
}
