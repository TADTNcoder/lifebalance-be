package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Payload used to update a role")
public class UpdateRoleRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Human-readable role name", example = "Manager", maxLength = 100)
    private String name;

    @Schema(description = "Role description", example = "Can manage users and review team resources")
    private String description;

    @Schema(description = "Whether this is a protected system role", example = "false")
    private Boolean system;

    @Schema(description = "Replacement permission id list assigned to the role")
    private List<UUID> permissionIds;
}
