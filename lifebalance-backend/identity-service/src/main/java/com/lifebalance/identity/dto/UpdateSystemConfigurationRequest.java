package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSystemConfigurationRequest {

    @NotBlank
    @Size(max = 10000)
    private String value;

    @Size(max = 1000)
    private String reason;

    private Boolean confirmed;
}
