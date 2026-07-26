package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    private Boolean system;

    private List<UUID> permissionIds;
}
