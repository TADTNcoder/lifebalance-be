package com.lifebalance.identity.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssignSupportTicketRequest {

    @NotNull
    private UUID assigneeId;

    @Size(max = 1000)
    private String reason;
}
