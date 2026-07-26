package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class RoleResponse {

    private UUID id;

    private String code;

    private String name;

    private String description;

    private Boolean system;

    private List<PermissionResponse> permissions;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

}
