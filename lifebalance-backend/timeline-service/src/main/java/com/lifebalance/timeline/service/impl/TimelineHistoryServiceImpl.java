package com.lifebalance.timeline.service.impl;

import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.dto.TimelineHistoryResponse;
import com.lifebalance.timeline.repository.TimelineHistoryRepository;
import com.lifebalance.timeline.service.TimelineHistoryService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TimelineHistoryServiceImpl implements TimelineHistoryService {

    private final TimelineHistoryRepository historyRepository;
    private final TimelineMapper mapper;

    TimelineHistoryServiceImpl(TimelineHistoryRepository historyRepository, TimelineMapper mapper) {
        this.historyRepository = historyRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimelineHistoryResponse> getHistory(
            UUID ownerId,
            UUID placementId,
            UUID taskId,
            TimelineHistoryActionType actionType,
            Pageable pageable
    ) {
        return historyRepository
                .search(ownerId, placementId, taskId, actionType, pageable)
                .map(mapper::toHistoryResponse);
    }
}
