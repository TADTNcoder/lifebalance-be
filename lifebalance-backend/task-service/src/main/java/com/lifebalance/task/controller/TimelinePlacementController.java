package com.lifebalance.task.controller;

import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.task.dto.request.CancelTimelinePlacementRequest;
import com.lifebalance.task.dto.request.RescheduleTimelinePlacementRequest;
import com.lifebalance.task.dto.request.ScheduleTimelinePlacementRequest;
import com.lifebalance.task.dto.response.TimelinePlacementResponse;
import com.lifebalance.task.service.TimelinePlacementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/timeline")
@RequiredArgsConstructor
public class TimelinePlacementController {

    private final TimelinePlacementService timelinePlacementService;

    @PostMapping("/placements")
    public TimelinePlacementResponse schedule(
            @Valid @RequestBody ScheduleTimelinePlacementRequest request,
            HttpServletRequest httpRequest) {

        return timelinePlacementService.schedule(
                AuthenticatedUserId.from(httpRequest),
                request);
    }

    @PatchMapping("/placements/{placementId}/reschedule")
    public TimelinePlacementResponse reschedule(
            @PathVariable UUID placementId,
            @Valid @RequestBody RescheduleTimelinePlacementRequest request,
            HttpServletRequest httpRequest) {

        return timelinePlacementService.reschedule(
                AuthenticatedUserId.from(httpRequest),
                placementId,
                request);
    }

    @PatchMapping("/placements/{placementId}/move")
    public TimelinePlacementResponse move(
            @PathVariable UUID placementId,
            @Valid @RequestBody RescheduleTimelinePlacementRequest request,
            HttpServletRequest httpRequest) {

        return timelinePlacementService.move(
                AuthenticatedUserId.from(httpRequest),
                placementId,
                request);
    }

    @PatchMapping("/placements/{placementId}/cancel")
    public void cancel(
            @PathVariable UUID placementId,
            @Valid @RequestBody(required = false) CancelTimelinePlacementRequest request,
            HttpServletRequest httpRequest) {

        timelinePlacementService.cancel(
                AuthenticatedUserId.from(httpRequest),
                placementId,
                request);
    }

    @GetMapping("/placements")
    public Page<TimelinePlacementResponse> getTimeline(
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest) {

        Pageable pageable = PageableLimits.of(page, size);
        return timelinePlacementService.getTimeline(
                AuthenticatedUserId.from(httpRequest),
                from,
                to,
                pageable);
    }

}
