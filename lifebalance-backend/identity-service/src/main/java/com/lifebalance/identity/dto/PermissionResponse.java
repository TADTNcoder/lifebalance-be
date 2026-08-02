package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "Permission details")
public class PermissionResponse {

    @Schema(description = "Permission id", example = "4b22f3c9-6e0d-47ad-846d-39946f235bf7")
    private UUID id;

    @Schema(description = "Unique machine-readable permission code", example = "task:create")
    private String code;

    @Schema(description = "Human-readable permission name", example = "Create task")
    private String name;

    @Schema(description = "Owning module", example = "task")
    private String module;

    @Schema(description = "Permission description", example = "Allows creating tasks")
    private String description;

    @Schema(description = "Whether this is a protected system permission", example = "false")
    private Boolean system;

    @Schema(description = "Creation timestamp", example = "2026-07-27T09:30:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-07-27T10:15:00+07:00")
    private OffsetDateTime updatedAt;
}
