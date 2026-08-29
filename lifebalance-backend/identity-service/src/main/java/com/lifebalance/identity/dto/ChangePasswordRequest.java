package com.lifebalance.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credentials used to securely change the authenticated user's password")
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    @Size(max = 128, message = "Current password must not exceed 128 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY, maxLength = 128)
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 12, max = 128, message = "New password must contain between 12 and 128 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY, minLength = 12, maxLength = 128)
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    @Size(max = 128, message = "Password confirmation must not exceed 128 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(format = "password", accessMode = Schema.AccessMode.WRITE_ONLY, maxLength = 128)
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
