package com.lifebalance.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCommentRequest {

    @NotBlank
    @Size(max = 5000)
    private String comment;
}
