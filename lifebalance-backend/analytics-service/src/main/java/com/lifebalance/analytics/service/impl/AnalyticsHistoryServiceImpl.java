package com.lifebalance.analytics.service.impl;

import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.dto.AnalyticsHistoryResponse;
import com.lifebalance.analytics.repository.AnalyticsHistoryRepository;
import com.lifebalance.analytics.service.AnalyticsHistoryService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AnalyticsHistoryServiceImpl implements AnalyticsHistoryService {

    private final AnalyticsHistoryRepository historyRepository;
    private final AnalyticsMapper mapper;

    AnalyticsHistoryServiceImpl(AnalyticsHistoryRepository historyRepository, AnalyticsMapper mapper) {
        this.historyRepository = historyRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnalyticsHistoryResponse> search(
            UUID ownerId,
            AnalyticsHistoryActionType actionType,
            UUID actualRecordId,
            UUID evaluationResultId,
            UUID reportId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    ) {
        ActualRecordServiceImpl.validateOwner(ownerId);
        if (from != null && to != null && from.isAfter(to)) {
            throw com.lifebalance.analytics.error.AnalyticsExceptions.invalidPeriod(
                    "from must be before or equal to to."
            );
        }
        return historyRepository.search(ownerId, actionType, actualRecordId, evaluationResultId, reportId, from, to, pageable)
                .map(mapper::toResponse);
    }
}
