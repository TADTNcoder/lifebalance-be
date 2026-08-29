package com.lifebalance.identity.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.config.OpenApiConfig;
import com.lifebalance.identity.dto.ChangePasswordRequest;
import com.lifebalance.identity.dto.PasswordChangeResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.PasswordChangeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Password", description = "Authenticated password management APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping({"/users/me/password", "/api/users/me/password"})
@RequiredArgsConstructor
public class PasswordController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordController.class);

    private final InternalUserService internalUserService;
    private final KeycloakUserMappingService keycloakUserMappingService;
    private final PasswordChangeService passwordChangeService;
    private final AuditLogService auditLogService;

    @Operation(
            summary = "Change current user's password",
            description = "Re-authenticates the current user, updates the Keycloak credential, and requires a new login"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Current password or password policy validation failed"),
            @ApiResponse(responseCode = "401", description = "Bearer authentication is required"),
            @ApiResponse(responseCode = "429", description = "Too many password verification attempts"),
            @ApiResponse(responseCode = "503", description = "Password service is unavailable")
    })
    @PutMapping
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest servletRequest,
            @Valid @RequestBody ChangePasswordRequest changeRequest
    ) {
        CurrentUser currentUser = keycloakUserMappingService.map(jwt);
        User user = internalUserService.findOrCreate(currentUser);
        String clientAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");

        try {
            PasswordChangeService.Result result = passwordChangeService.changePassword(
                    user,
                    currentUser.getUsername(),
                    changeRequest,
                    clientAddress
            );
            saveAuditSafely(
                    user,
                    AuditStatus.SUCCESS,
                    clientAddress,
                    userAgent,
                    "Password changed; reauthentication required"
            );
            PasswordChangeResponse response = new PasswordChangeResponse(true, result.sessionsRevoked());
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(response);
        } catch (RuntimeException exception) {
            String errorCode = exception instanceof AppException appException
                    ? appException.getCode()
                    : "UNEXPECTED_ERROR";
            saveAuditSafely(
                    user,
                    AuditStatus.FAILED,
                    clientAddress,
                    userAgent,
                    "Password change failed (" + errorCode + ')'
            );
            throw exception;
        }
    }

    private void saveAuditSafely(
            User user,
            AuditStatus status,
            String clientAddress,
            String userAgent,
            String details
    ) {
        try {
            auditLogService.saveAudit(
                    user,
                    AuditAction.CHANGE_PASSWORD,
                    status,
                    clientAddress,
                    userAgent,
                    details
            );
        } catch (RuntimeException exception) {
            LOGGER.error("Could not persist password-change audit event", exception);
        }
    }
}
