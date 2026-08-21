package com.lifebalance.analytics.service;

import com.lifebalance.analytics.domain.AnalyticsHistoryActionType;
import com.lifebalance.analytics.dto.AnalyticsHistoryResponse;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnalyticsHistoryService {

    Page<AnalyticsHistoryResponse> search(
            UUID ownerId,
            AnalyticsHistoryActionType actionType,
            UUID actualRecordId,
            UUID evaluationResultId,
            UUID reportId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );
}
