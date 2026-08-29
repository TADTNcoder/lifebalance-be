package com.lifebalance.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a successful password change")
public record PasswordChangeResponse(
        @Schema(description = "The client must discard its current session and authenticate again")
        boolean reauthenticationRequired,
        @Schema(description = "Whether Keycloak accepted the request to revoke the user's sessions")
        boolean sessionsRevoked
) {
}
