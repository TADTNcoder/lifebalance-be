package com.lifebalance.task.service;

import com.lifebalance.task.dto.request.CancelTimelinePlacementRequest;
import com.lifebalance.task.dto.request.RescheduleTimelinePlacementRequest;
import com.lifebalance.task.dto.request.ScheduleTimelinePlacementRequest;
import com.lifebalance.task.dto.response.TimelinePlacementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TimelinePlacementService {

    TimelinePlacementResponse schedule(
            UUID ownerId,
            ScheduleTimelinePlacementRequest request);

    TimelinePlacementResponse reschedule(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request);

    TimelinePlacementResponse move(
            UUID ownerId,
            UUID placementId,
            RescheduleTimelinePlacementRequest request);

    void cancel(
            UUID ownerId,
            UUID placementId,
            CancelTimelinePlacementRequest request);

    Page<TimelinePlacementResponse> getTimeline(
            UUID ownerId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable);
}
