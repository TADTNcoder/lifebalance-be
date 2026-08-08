package com.lifebalance.task.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class CategoryResponse {

    private UUID id;

    private String name;

    private String slug;

    private String description;

    private String color;

    private String icon;

    private Boolean isSystem;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
