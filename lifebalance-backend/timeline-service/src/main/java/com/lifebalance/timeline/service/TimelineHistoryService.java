package com.lifebalance.timeline.service;

import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.dto.TimelineHistoryResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimelineHistoryService {

    Page<TimelineHistoryResponse> getHistory(
            UUID ownerId,
            UUID placementId,
            UUID taskId,
            TimelineHistoryActionType actionType,
            Pageable pageable);
}
