package com.lifebalance.timeline.service;

import com.lifebalance.timeline.dto.TimelineTaskResponse;
import com.lifebalance.timeline.dto.UpsertTimelineTaskRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimelineTaskService {

    TimelineTaskResponse upsertTask(UUID ownerId, UpsertTimelineTaskRequest request);

    TimelineTaskResponse getTask(UUID ownerId, UUID taskId);

    Page<TimelineTaskResponse> getEligibleTasks(UUID ownerId, Pageable pageable);
}
