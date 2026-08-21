package com.lifebalance.timeline.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.timeline.domain.TimelinePlacementStatus;
import com.lifebalance.timeline.dto.CancelTimelinePlacementRequest;
import com.lifebalance.timeline.dto.PageResponse;
import com.lifebalance.timeline.dto.RescheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.ScheduleTimelinePlacementRequest;
import com.lifebalance.timeline.dto.TimelineAvailabilityResponse;
import com.lifebalance.timeline.dto.TimelinePlacementResponse;
import com.lifebalance.timeline.error.TimelineExceptions;
import com.lifebalance.timeline.service.TimelinePlacementService;
import jakarta.validation.Valid;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline")
public class TimelinePlacementController {

    private static final String DEFAULT_TIMEZONE = "UTC";

    private final TimelinePlacementService timelinePlacementService;

    public TimelinePlacementController(TimelinePlacementService timelinePlacementService) {
        this.timelinePlacementService = timelinePlacementService;
    }

    @PostMapping("/placements")
    public ResponseEntity<ApiResponse<TimelinePlacementResponse>> schedule(
            @Valid @RequestBody ScheduleTimelinePlacementRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        TimelinePlacementResponse response = timelinePlacementService.schedule(
                CurrentTimelineUser.ownerId(currentUser),
                request
        );
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PatchMapping("/placements/{placementId}/reschedule")
    public ResponseEntity<ApiResponse<TimelinePlacementResponse>> reschedule(
            @PathVariable UUID placementId,
            @Valid @RequestBody RescheduleTimelinePlacementRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(timelinePlacementService.reschedule(
                CurrentTimelineUser.ownerId(currentUser),
                placementId,
                request
        )));
    }

    @PatchMapping("/placements/{placementId}/move")
    public ResponseEntity<ApiResponse<TimelinePlacementResponse>> move(
            @PathVariable UUID placementId,
            @Valid @RequestBody RescheduleTimelinePlacementRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(timelinePlacementService.move(
                CurrentTimelineUser.ownerId(currentUser),
                placementId,
                request
        )));
    }

    @PatchMapping("/placements/{placementId}/cancel")
    public ResponseEntity<ApiResponse<TimelinePlacementResponse>> cancel(
            @PathVariable UUID placementId,
            @Valid @RequestBody(required = false) CancelTimelinePlacementRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(timelinePlacementService.cancel(
                CurrentTimelineUser.ownerId(currentUser),
                placementId,
                request
        )));
    }

    @PatchMapping("/placements/{placementId}/archive")
    public ResponseEntity<ApiResponse<TimelinePlacementResponse>> archive(
            @PathVariable UUID placementId,
            @Valid @RequestBody(required = false) CancelTimelinePlacementRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(timelinePlacementService.archive(
                CurrentTimelineUser.ownerId(currentUser),
                placementId,
                request
        )));
    }

    @GetMapping("/placements/{placementId}")
    public ResponseEntity<ApiResponse<TimelinePlacementResponse>> getById(
            @PathVariable UUID placementId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(timelinePlacementService.getById(
                CurrentTimelineUser.ownerId(currentUser),
                placementId
        )));
    }

    @GetMapping("/placements")
    public ResponseEntity<ApiResponse<PageResponse<TimelinePlacementResponse>>> getTimeline(
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to,
            @RequestParam(required = false) TimelinePlacementStatus status,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(timelinePlacementService.getTimeline(
                CurrentTimelineUser.ownerId(currentUser),
                from,
                to,
                status,
                PageableLimits.normalize(pageable)
        ))));
    }

    @GetMapping("/day")
    public ResponseEntity<ApiResponse<PageResponse<TimelinePlacementResponse>>> getDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = DEFAULT_TIMEZONE) String timezone,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        OffsetDateTime from = atStartOfDay(date, timezone);
        return timelineWindow(from, from.plusDays(1), pageable, currentUser);
    }

    @GetMapping("/week")
    public ResponseEntity<ApiResponse<PageResponse<TimelinePlacementResponse>>> getWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(defaultValue = DEFAULT_TIMEZONE) String timezone,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        OffsetDateTime from = atStartOfDay(weekStart, timezone);
        return timelineWindow(from, from.plusDays(7), pageable, currentUser);
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<TimelineAvailabilityResponse>> getAvailability(
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(timelinePlacementService.getAvailability(
                CurrentTimelineUser.ownerId(currentUser),
                from,
                to
        )));
    }

    private ResponseEntity<ApiResponse<PageResponse<TimelinePlacementResponse>>> timelineWindow(
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable,
            KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(timelinePlacementService.getTimeline(
                CurrentTimelineUser.ownerId(currentUser),
                from,
                to,
                TimelinePlacementStatus.ACTIVE,
                PageableLimits.normalize(pageable)
        ))));
    }

    private static OffsetDateTime atStartOfDay(LocalDate date, String timezone) {
        try {
            return date.atStartOfDay(ZoneId.of(timezone)).toOffsetDateTime();
        } catch (DateTimeException exception) {
            throw TimelineExceptions.invalidTimezone(timezone);
        }
    }
}
