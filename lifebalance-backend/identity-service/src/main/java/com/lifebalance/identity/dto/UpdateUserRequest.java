package com.lifebalance.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 100)
    private String username;

    @Size(max = 255)
    private String displayName;

    @Email
    @Size(max = 255)
    private String email;
}
