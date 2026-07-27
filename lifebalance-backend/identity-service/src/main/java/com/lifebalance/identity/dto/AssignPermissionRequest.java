package com.lifebalance.identity.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignPermissionRequest {

    @NotNull
    private UUID permissionId;
}