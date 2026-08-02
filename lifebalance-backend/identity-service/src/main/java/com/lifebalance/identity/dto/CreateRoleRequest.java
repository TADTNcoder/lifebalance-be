package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Payload used to create a role")
public class CreateRoleRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Unique machine-readable role code", example = "MANAGER", maxLength = 100)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Human-readable role name", example = "Manager", maxLength = 255)
    private String name;

    @Schema(description = "Role description", example = "Can manage users and review team resources")
    private String description;

    @Schema(description = "Whether this is a protected system role", example = "false", defaultValue = "false")
    private Boolean system = false;

    @Schema(description = "Permission ids assigned to the role")
    private List<UUID> permissionIds;

}
