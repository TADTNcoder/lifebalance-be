package com.lifebalance.notification.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationDeliveryStatus;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.domain.NotificationStatus;
import com.lifebalance.notification.dto.BroadcastNotificationRequest;
import com.lifebalance.notification.dto.BulkNotificationActionResponse;
import com.lifebalance.notification.dto.CreateNotificationRequest;
import com.lifebalance.notification.dto.NotificationReasonRequest;
import com.lifebalance.notification.dto.NotificationResponse;
import com.lifebalance.notification.dto.PageResponse;
import com.lifebalance.notification.dto.UnreadCountResponse;
import com.lifebalance.notification.service.NotificationService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    static final String INTERNAL_SECRET_HEADER = "X-Lifebalance-Internal-Secret";

    private final NotificationService notificationService;
    private final String internalSecret;

    public NotificationController(
            NotificationService notificationService,
            @Value("${lifebalance.integration.internal-secret:}") String internalSecret
    ) {
        this.notificationService = notificationService;
        this.internalSecret = trimToEmpty(internalSecret);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> create(
            @Valid @RequestBody CreateNotificationRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser,
            @RequestHeader(value = INTERNAL_SECRET_HEADER, required = false) String submittedInternalSecret
    ) {
        CurrentNotificationUser.requireAllowedChannels(
                currentUser,
                request.channels(),
                isTrustedInternalRequest(submittedInternalSecret)
        );
        List<NotificationResponse> response = notificationService.create(
                CurrentNotificationUser.ownerId(currentUser),
                request
        );
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> broadcast(
            @Valid @RequestBody BroadcastNotificationRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser,
            @RequestHeader(value = INTERNAL_SECRET_HEADER, required = false) String submittedInternalSecret
    ) {
        CurrentNotificationUser.requireNotificationManager(
                currentUser,
                isTrustedInternalRequest(submittedInternalSecret)
        );
        List<NotificationResponse> response = notificationService.broadcast(
                CurrentNotificationUser.ownerId(currentUser),
                request
        );
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(
            @PathVariable UUID notificationId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getById(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> search(
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationEventType eventType,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) NotificationDeliveryStatus deliveryStatus,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) UUID referenceId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(notificationService.search(
                CurrentNotificationUser.ownerId(currentUser),
                status,
                eventType,
                channel,
                deliveryStatus,
                referenceType,
                referenceId,
                from,
                to,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @PathVariable UUID notificationId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markRead(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId
        )));
    }

    @PatchMapping("/{notificationId}/unread")
    public ResponseEntity<ApiResponse<NotificationResponse>> markUnread(
            @PathVariable UUID notificationId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markUnread(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId
        )));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<BulkNotificationActionResponse>> markAllRead(
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAllRead(
                CurrentNotificationUser.ownerId(currentUser)
        )));
    }

    @PatchMapping("/{notificationId}/archive")
    public ResponseEntity<ApiResponse<NotificationResponse>> archive(
            @PathVariable UUID notificationId,
            @Valid @RequestBody(required = false) NotificationReasonRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.archive(
                CurrentNotificationUser.ownerId(currentUser),
                notificationId,
                request
        )));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> unreadCount(
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.unreadCount(
                CurrentNotificationUser.ownerId(currentUser)
        )));
    }

    private boolean isTrustedInternalRequest(String submittedInternalSecret) {
        String submitted = trimToEmpty(submittedInternalSecret);
        if (internalSecret.isEmpty() || submitted.isEmpty()) {
            return false;
        }
        return MessageDigest.isEqual(
                internalSecret.getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
