package com.lifebalance.timeline.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.timeline.dto.PageResponse;
import com.lifebalance.timeline.dto.TimelineTaskResponse;
import com.lifebalance.timeline.dto.UpsertTimelineTaskRequest;
import com.lifebalance.timeline.service.TimelineTaskService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline/tasks")
public class TimelineTaskController {

    private final TimelineTaskService timelineTaskService;

    public TimelineTaskController(TimelineTaskService timelineTaskService) {
        this.timelineTaskService = timelineTaskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimelineTaskResponse>> upsertTask(
            @Valid @RequestBody UpsertTimelineTaskRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                timelineTaskService.upsertTask(CurrentTimelineUser.ownerId(currentUser), request)));
    }

    @GetMapping("/eligible")
    public ResponseEntity<ApiResponse<PageResponse<TimelineTaskResponse>>> getEligibleTasks(
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(timelineTaskService.getEligibleTasks(
                CurrentTimelineUser.ownerId(currentUser),
                PageableLimits.normalize(pageable)
        ))));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TimelineTaskResponse>> getTask(
            @PathVariable UUID taskId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                timelineTaskService.getTask(CurrentTimelineUser.ownerId(currentUser), taskId)));
    }
}
