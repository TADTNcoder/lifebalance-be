package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Payload used to create a permission")
public class CreatePermissionRequest {

    @NotBlank
    @Size(max = 150)
    @Schema(description = "Unique machine-readable permission code", example = "task:create", maxLength = 150)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Human-readable permission name", example = "Create task", maxLength = 255)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Owning module", example = "task", maxLength = 100)
    private String module;

    @Schema(description = "Permission description", example = "Allows creating tasks")
    private String description;

    @Schema(description = "Whether this is a protected system permission", example = "false", defaultValue = "false")
    private Boolean system = false;
}
