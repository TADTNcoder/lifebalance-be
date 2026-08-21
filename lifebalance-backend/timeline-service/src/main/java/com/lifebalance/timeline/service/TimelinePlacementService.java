package com.lifebalance.timeline.service;

import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.dto.CancelTimelinePlacementRequest;
import com.lifebalance.timeline.dto.RescheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.ScheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.TimelineAvailabilityResponse;
import com.lifebalance.timeline.dto.TimelinePlacementResponse;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimelinePlacementService {

    TimelinePlacementResponse schedule(UUID ownerId, ScheduleTimelinePlacementRequest request);

    TimelinePlacementResponse reschedule(UUID ownerId, UUID placementId, RescheduleTimelinePlacementRequest request);

    TimelinePlacementResponse move(UUID ownerId, UUID placementId, RescheduleTimelinePlacementRequest request);

    TimelinePlacementResponse cancel(UUID ownerId, UUID placementId, CancelTimelinePlacementRequest request);

    TimelinePlacementResponse archive(UUID ownerId, UUID placementId, CancelTimelinePlacementRequest request);

    TimelinePlacementResponse getById(UUID ownerId, UUID placementId);

    Page<TimelinePlacementResponse> getTimeline(
            UUID ownerId,
            OffsetDateTime from,
            OffsetDateTime to,
            TimelinePlacementStatus status,
            Pageable pageable);

    TimelineAvailabilityResponse getAvailability(UUID ownerId, OffsetDateTime from, OffsetDateTime to);
}
