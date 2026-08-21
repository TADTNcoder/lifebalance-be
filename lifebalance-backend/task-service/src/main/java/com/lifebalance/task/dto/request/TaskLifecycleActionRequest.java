package com.lifebalance.task.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskLifecycleActionRequest {

    @Size(max = 500)
    private String reason;
}
