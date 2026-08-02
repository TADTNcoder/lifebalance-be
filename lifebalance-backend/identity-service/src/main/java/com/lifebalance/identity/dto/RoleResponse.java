package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Role details with assigned permissions")
public class RoleResponse {

    @Schema(description = "Role id", example = "2e1be6d8-a7bb-4541-9720-f7e4b119d780")
    private UUID id;

    @Schema(description = "Unique machine-readable role code", example = "MANAGER")
    private String code;

    @Schema(description = "Human-readable role name", example = "Manager")
    private String name;

    @Schema(description = "Role description", example = "Can manage users and review team resources")
    private String description;

    @Schema(description = "Whether this is a protected system role", example = "false")
    private Boolean system;

    @Schema(description = "Permissions assigned to the role")
    private List<PermissionResponse> permissions;

    @Schema(description = "Creation timestamp", example = "2026-07-27T09:30:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-07-27T10:15:00+07:00")
    private OffsetDateTime updatedAt;

}
