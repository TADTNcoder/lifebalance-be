package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CapitalHistoryResponse(
        UUID id,
        UUID capitalCycleId,
        CapitalKind capitalType,
        CapitalActionType actionType,
        BigDecimal amount,
        BigDecimal beforeAmount,
        BigDecimal afterAmount,
        String reason,
        String description,
        CapitalReferenceType referenceType,
        UUID referenceId,
        CapitalActorType actorType,
        UUID actorId,
        Instant createdAt
) {
}
