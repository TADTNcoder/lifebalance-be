package com.lifebalance.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTagRequest {

    @NotBlank
    private String name;

    private String description;
}