package com.lifebalance.identity.exception;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.model.enums.SupportTicketStatus;

public class AdministrationSupportException extends AppException {

    private AdministrationSupportException(
            String code,
            String message,
            HttpStatus status,
            Map<String, String> details
    ) {
        super(code, message, status, details);
    }

    public static AdministrationSupportException validation(
            String message
    ) {
        return new AdministrationSupportException(
                IdentityErrorCode.ADMINISTRATION_VALIDATION_FAILED,
                message,
                HttpStatus.BAD_REQUEST,
                Map.of()
        );
    }

    public static AdministrationSupportException ticketNotFound(UUID ticketId) {
        return new AdministrationSupportException(
                IdentityErrorCode.SUPPORT_TICKET_NOT_FOUND,
                "Support ticket not found: " + ticketId,
                HttpStatus.NOT_FOUND,
                Map.of("ticketId", String.valueOf(ticketId))
        );
    }

    public static AdministrationSupportException announcementNotFound(UUID announcementId) {
        return new AdministrationSupportException(
                IdentityErrorCode.ANNOUNCEMENT_NOT_FOUND,
                "System announcement not found: " + announcementId,
                HttpStatus.NOT_FOUND,
                Map.of("announcementId", String.valueOf(announcementId))
        );
    }

    public static AdministrationSupportException invalidTicketState(
            UUID ticketId,
            SupportTicketStatus currentStatus,
            String action
    ) {
        return new AdministrationSupportException(
                IdentityErrorCode.SUPPORT_TICKET_INVALID_STATE,
                "Support ticket cannot be " + action + " from status " + currentStatus,
                HttpStatus.CONFLICT,
                Map.of(
                        "ticketId", String.valueOf(ticketId),
                        "currentStatus", String.valueOf(currentStatus),
                        "action", action
                )
        );
    }

    public static AdministrationSupportException ticketScopeDenied(UUID ticketId) {
        return new AdministrationSupportException(
                IdentityErrorCode.SUPPORT_TICKET_SCOPE_DENIED,
                "Support ticket is outside the actor scope: " + ticketId,
                HttpStatus.FORBIDDEN,
                Map.of("ticketId", String.valueOf(ticketId))
        );
    }

    public static AdministrationSupportException operationForbidden(String action) {
        return new AdministrationSupportException(
                IdentityErrorCode.SUPPORT_TICKET_SCOPE_DENIED,
                "Actor is not allowed to perform administration action: " + action,
                HttpStatus.FORBIDDEN,
                Map.of("action", action)
        );
    }

    public static AdministrationSupportException configurationNotFound(String configKey) {
        return new AdministrationSupportException(
                IdentityErrorCode.CONFIGURATION_NOT_FOUND,
                "System configuration not found: " + configKey,
                HttpStatus.NOT_FOUND,
                Map.of("configKey", String.valueOf(configKey))
        );
    }

    public static AdministrationSupportException configurationUpdateNotAllowed(String configKey, String reason) {
        return new AdministrationSupportException(
                IdentityErrorCode.CONFIGURATION_UPDATE_NOT_ALLOWED,
                "System configuration cannot be updated: " + configKey,
                HttpStatus.CONFLICT,
                Map.of(
                        "configKey", String.valueOf(configKey),
                        "reason", reason
                )
        );
    }
}
