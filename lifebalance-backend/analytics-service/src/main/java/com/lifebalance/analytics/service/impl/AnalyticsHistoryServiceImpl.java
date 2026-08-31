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

    private static final OffsetDateTime EARLIEST_HISTORY_DATE =
            OffsetDateTime.parse("0001-01-01T00:00:00Z");
    private static final OffsetDateTime LATEST_HISTORY_DATE =
            OffsetDateTime.parse("9999-12-31T23:59:59.999999999Z");

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
        OffsetDateTime normalizedFrom = from == null ? EARLIEST_HISTORY_DATE : from;
        OffsetDateTime normalizedTo = to == null ? LATEST_HISTORY_DATE : to;

        return historyRepository.search(
                        ownerId,
                        actionType,
                        actualRecordId,
                        evaluationResultId,
                        reportId,
                        normalizedFrom,
                        normalizedTo,
                        pageable
                )
                .map(mapper::toResponse);
    }
}
