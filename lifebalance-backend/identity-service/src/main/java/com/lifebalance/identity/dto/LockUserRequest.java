package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Payload used to lock a user account")
public class LockUserRequest {

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Reason shown in audit and account status", example = "Suspicious login activity", maxLength = 1000)
    private String reason;

    @Future
    @Schema(description = "Optional future unlock deadline", example = "2026-08-01T09:00:00+07:00")
    private OffsetDateTime lockedUntil;
}
