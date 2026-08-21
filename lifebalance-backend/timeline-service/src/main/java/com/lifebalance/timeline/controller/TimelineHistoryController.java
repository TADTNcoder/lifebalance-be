package com.lifebalance.timeline.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.timeline.domain.TimelineHistoryActionType;
import com.lifebalance.timeline.dto.PageResponse;
import com.lifebalance.timeline.dto.TimelineHistoryResponse;
import com.lifebalance.timeline.service.TimelineHistoryService;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/timeline/history")
public class TimelineHistoryController {

    private final TimelineHistoryService timelineHistoryService;

    public TimelineHistoryController(TimelineHistoryService timelineHistoryService) {
        this.timelineHistoryService = timelineHistoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TimelineHistoryResponse>>> getHistory(
            @RequestParam(required = false) UUID placementId,
            @RequestParam(required = false) UUID taskId,
            @RequestParam(required = false) TimelineHistoryActionType actionType,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(timelineHistoryService.getHistory(
                CurrentTimelineUser.ownerId(currentUser),
                placementId,
                taskId,
                actionType,
                PageableLimits.normalize(pageable)
        ))));
    }
}
