package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateRoleRequest {

    @NotBlank
    @Size(max = 100)
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    private Boolean system = false;

    private List<UUID> permissionIds;

}
