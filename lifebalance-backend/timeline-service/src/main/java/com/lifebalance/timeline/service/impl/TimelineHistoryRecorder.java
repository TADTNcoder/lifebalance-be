package com.lifebalance.timeline.service.impl;

import com.lifebalance.timeline.domain.TimelineHistory;
import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.domain.TimelinePlacement;
import com.lifebalance.timeline.domain.TimelineTask;
import com.lifebalance.timeline.repository.TimelineHistoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class TimelineHistoryRecorder {

    private final TimelineHistoryRepository historyRepository;

    TimelineHistoryRecorder(TimelineHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    void record(
            UUID ownerId,
            UUID actorId,
            TimelineHistoryActionType actionType,
            TimelinePlacement placement,
            TimelineTask task,
            String oldValue,
            String newValue,
            String reason
    ) {
        historyRepository.save(TimelineHistory.record(
                ownerId,
                actorId,
                actionType,
                placement,
                task,
                oldValue,
                newValue,
                reason
        ));
    }
}
