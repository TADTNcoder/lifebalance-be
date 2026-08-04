package com.lifebalance.resourcecapital.service;

import com.lifebalance.resourcecapital.domain.capital.CapitalKind;
import com.lifebalance.resourcecapital.dto.CapitalHistoryResponse;
import com.lifebalance.resourcecapital.dto.HistoryFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CapitalHistoryService {

    Page<CapitalHistoryResponse> getHistoryByCycle(
            UUID ownerId,
            UUID cycleId,
            HistoryFilterRequest filter,
            Pageable pageable
    );

    Page<CapitalHistoryResponse> getHistoryByResource(
            UUID ownerId,
            UUID cycleId,
            CapitalKind capitalType,
            Pageable pageable
    );
}
