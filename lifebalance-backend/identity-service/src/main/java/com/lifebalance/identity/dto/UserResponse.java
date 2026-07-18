package com.lifebalance.identity.dto;

import java.util.UUID;

import com.lifebalance.identity.model.enums.AccountStatus;

import lombok.Data;

@Data
public class UserResponse {

    private UUID id;

    private String email;

    private String username;

    private String displayName;

    private AccountStatus status;
}
