package com.lifebalance.task.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskProgressRequest {

    @NotNull
    @Min(0)
    @Max(100)
    private Integer progress;

    @Size(max = 500)
    private String reason;
}
