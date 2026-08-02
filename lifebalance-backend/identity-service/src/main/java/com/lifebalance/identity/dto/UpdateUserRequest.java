package com.lifebalance.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Payload used to update user profile fields")
public class UpdateUserRequest {

    @Size(max = 100)
    @Schema(description = "Username", example = "alice", maxLength = 100)
    private String username;

    @Size(max = 255)
    @Schema(description = "Display name", example = "Alice Nguyen", maxLength = 255)
    private String displayName;

    @Email
    @Size(max = 255)
    @Schema(description = "Email address", example = "alice@example.com", maxLength = 255)
    private String email;
}
