package com.lifebalance.identity.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.AccountStatus;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "User account details")
public class UserResponse {

    @Schema(description = "User id", example = "6f44f86a-66df-4b0d-b258-571a3a63fce1")
    private UUID id;

    @Schema(description = "Email address", example = "alice@example.com")
    private String email;

    @Schema(description = "Username", example = "alice")
    private String username;

    @Schema(description = "Display name", example = "Alice Nguyen")
    private String displayName;

    @Schema(description = "Phone number", example = "+84 912 345 678")
    private String phone;

    @Schema(description = "Gender", example = "Nữ")
    private String gender;

    @Schema(description = "Birth date", example = "1998-05-20")
    private LocalDate birthDate;

    @Schema(description = "Account status", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Registration timestamp", example = "2026-07-27T09:30:00+07:00")
    private OffsetDateTime registeredAt;

    @Schema(description = "Last successful login timestamp", example = "2026-07-27T10:15:00+07:00")
    private OffsetDateTime lastLoginAt;

    @Schema(description = "Reason for the current lock, if any", example = "Suspicious login activity")
    private String lockReason;

    @Schema(description = "Lock timestamp, if the account is locked", example = "2026-07-27T10:15:00+07:00")
    private OffsetDateTime lockedAt;

    @Schema(description = "Optional lock expiration timestamp", example = "2026-08-01T09:00:00+07:00")
    private OffsetDateTime lockedUntil;
}
