package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    private Boolean system = false;
}
