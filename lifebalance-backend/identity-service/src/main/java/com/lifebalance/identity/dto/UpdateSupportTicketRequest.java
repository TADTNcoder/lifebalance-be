package com.lifebalance.identity.dto;

import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSupportTicketRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    private SupportTicketPriority priority;

    private SupportTicketCategory category;

    @Size(max = 1000)
    private String reason;
}
