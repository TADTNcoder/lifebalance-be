package com.lifebalance.notification.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.notification.domain.NotificationHistoryActionType;
import com.lifebalance.notification.dto.NotificationHistoryResponse;
import com.lifebalance.notification.dto.PageResponse;
import com.lifebalance.notification.service.NotificationHistoryService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/history")
public class NotificationHistoryController {

    private final NotificationHistoryService historyService;

    public NotificationHistoryController(NotificationHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationHistoryResponse>>> getHistory(
            @RequestParam(required = false) UUID notificationId,
            @RequestParam(required = false) NotificationHistoryActionType actionType,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(historyService.getHistory(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId,
                actionType,
                PageableLimits.normalize(pageable)
        ))));
    }
}
