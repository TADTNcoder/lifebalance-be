package com.lifebalance.notification.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.common.web.PageableLimits;
import com.lifebalance.notification.domain.NotificationChannel;
import com.lifebalance.notification.domain.NotificationEventType;
import com.lifebalance.notification.dto.CreateNotificationTemplateRequest;
import com.lifebalance.notification.dto.NotificationTemplateResponse;
import com.lifebalance.notification.dto.PageResponse;
import com.lifebalance.notification.dto.UpdateNotificationTemplateRequest;
import com.lifebalance.notification.service.NotificationTemplateService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/notifications/templates")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    public NotificationTemplateController(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> create(
            @Valid @RequestBody CreateNotificationTemplateRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        NotificationTemplateResponse response = templateService.create(
                CurrentNotificationUser.ownerId(currentUser),
                request
        );
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> getById(
            @PathVariable UUID templateId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getById(
                CurrentNotificationUser.ownerId(currentUser),
                templateId
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationTemplateResponse>>> search(
            @RequestParam(required = false) NotificationEventType eventType,
            @RequestParam(required = false) NotificationChannel channel,
            @RequestParam(required = false) Boolean enabled,
            Pageable pageable,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(templateService.search(
                CurrentNotificationUser.ownerId(currentUser),
                eventType,
                channel,
                enabled,
                PageableLimits.normalize(pageable)
        ))));
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> update(
            @PathVariable UUID templateId,
            @Valid @RequestBody UpdateNotificationTemplateRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.update(
                CurrentNotificationUser.ownerId(currentUser),
                templateId,
                request
        )));
    }

    @PatchMapping("/{templateId}/archive")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> archive(
            @PathVariable UUID templateId,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(templateService.archive(
                CurrentNotificationUser.ownerId(currentUser),
                templateId
        )));
    }
}
