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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline/tasks")
public class TimelineTaskController {

    static final String INTERNAL_SECRET_HEADER = "X-Lifebalance-Internal-Secret";

    private final TimelineTaskService timelineTaskService;
    private final String internalSecret;

    public TimelineTaskController(
            TimelineTaskService timelineTaskService,
            @Value("${lifebalance.integration.internal-secret:}") String internalSecret
    ) {
        this.timelineTaskService = timelineTaskService;
        this.internalSecret = trimToEmpty(internalSecret);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TimelineTaskResponse>> upsertTask(
            @Valid @RequestBody UpsertTimelineTaskRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser,
            @RequestHeader(value = INTERNAL_SECRET_HEADER, required = false) String submittedInternalSecret
    ) {
        requireTrustedInternalRequest(submittedInternalSecret);
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

    private void requireTrustedInternalRequest(String submittedInternalSecret) {
        String submitted = trimToEmpty(submittedInternalSecret);
        if (internalSecret.isEmpty() || submitted.isEmpty()
                || !MessageDigest.isEqual(
                internalSecret.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new AccessDeniedException("Internal service credential is required.");
        }
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
