package com.lifebalance.notification.controller;

import static com.lifebalance.security.keycloak.KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE;

import com.lifebalance.common.api.ApiResponse;
import com.lifebalance.notification.dto.NotificationPreferenceResponse;
import com.lifebalance.notification.dto.UpdateNotificationPreferenceRequest;
import com.lifebalance.notification.service.NotificationPreferenceService;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    public NotificationPreferenceController(NotificationPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationPreferenceResponse>>> getPreferences(
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getPreferences(
                CurrentNotificationUser.ownerId(currentUser)
        )));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> upsert(
            @Valid @RequestBody UpdateNotificationPreferenceRequest request,
            @RequestAttribute(value = CURRENT_USER_ATTRIBUTE, required = false) KeycloakUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.upsert(
                CurrentNotificationUser.ownerId(currentUser),
                request
        )));
    }
}
