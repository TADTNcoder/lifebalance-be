package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.SupportTicketStatus;
import com.lifebalance.identity.model.enums.TicketHistoryAction;

import lombok.Builder;

@Builder
public record SupportTicketHistoryResponse(
        UUID id,
        UUID ticketId,
        UUID actorId,
        String actorEmail,
        TicketHistoryAction action,
        SupportTicketStatus previousStatus,
        SupportTicketStatus newStatus,
        UUID previousAssigneeId,
        UUID newAssigneeId,
        String commentText,
        String reason,
        String oldValue,
        String newValue,
        OffsetDateTime createdAt
) {
}
