package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PermissionResponse {
    private UUID id;

    private String code;

    private String name;

    private String module;

    private String description;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
