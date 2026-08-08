package com.lifebalance.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    private static final String HEX_COLOR_PATTERN = "^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$";

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String slug;

    private String description;

    @Size(max = 20)
    @Pattern(regexp = HEX_COLOR_PATTERN, message = "Category color must be a valid hex color.")
    private String color;

    @Size(max = 50)
    private String icon;
}
