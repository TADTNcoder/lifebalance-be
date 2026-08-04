package com.lifebalance.resourcecapital.dto;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActionType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalActorType;
import com.lifebalance.resourcecapital.domain.capitalhistory.CapitalReferenceType;

import java.time.Instant;
import java.util.UUID;

public record HistoryFilterRequest(
        CapitalKind capitalType,
        CapitalActionType actionType,
        Instant from,
        Instant to,
        String keyword,
        CapitalReferenceType referenceType,
        UUID referenceId,
        CapitalActorType actorType,
        UUID actorId
) {
}
