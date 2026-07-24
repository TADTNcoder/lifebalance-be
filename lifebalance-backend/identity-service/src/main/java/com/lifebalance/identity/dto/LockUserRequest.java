package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LockUserRequest {

    @NotBlank
    @Size(max = 1000)
    private String reason;

    @Future
    private OffsetDateTime lockedUntil;
}
