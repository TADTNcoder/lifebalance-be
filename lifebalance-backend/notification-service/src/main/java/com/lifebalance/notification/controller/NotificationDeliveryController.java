package com.lifebalance.notification.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.dto.MarkDeliveryFailedRequest;
import com.lifebalance.notification.dto.MarkDeliverySentRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.PageResponse;
import com.lifebalance.notification.dto.RetryDeliveryRequest;
import com.lifebalance.notification.service.NotificationDeliveryService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/delivery")
public class NotificationDeliveryController {

    private final NotificationDeliveryService deliveryService;

    public NotificationDeliveryController(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getPending(
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) OffsetDateTime dueAt,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        CurrentNotificationUser.requireNotificationManager(currentUser, false);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(deliveryService.getPending(
                CurrentNotificationUser.ownerId(currentUser),
                channel,
                dueAt,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{notificationId}/sent")
    public ResponseEntity<ApiResponse<NotificationResponse>> markSent(
            @PathVariable UUID notificationId,
            @Valid @RequestBody(required = false) MarkDeliverySentRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        CurrentNotificationUser.requireNotificationManager(currentUser, false);
        return ResponseEntity.ok(ApiResponse.success(deliveryService.markSent(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId,
                request
        )));
    }

    @PatchMapping("/{notificationId}/failed")
    public ResponseEntity<ApiResponse<NotificationResponse>> markFailed(
            @PathVariable UUID notificationId,
            @Valid @RequestBody(required = false) MarkDeliveryFailedRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        CurrentNotificationUser.requireNotificationManager(currentUser, false);
        return ResponseEntity.ok(ApiResponse.success(deliveryService.markFailed(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId,
                request
        )));
    }

    @PatchMapping("/{notificationId}/retry")
    public ResponseEntity<ApiResponse<NotificationResponse>> retry(
            @PathVariable UUID notificationId,
            @Valid @RequestBody(required = false) RetryDeliveryRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        CurrentNotificationUser.requireNotificationManager(currentUser, false);
        return ResponseEntity.ok(ApiResponse.success(deliveryService.retry(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId,
                request
        )));
    }
}
