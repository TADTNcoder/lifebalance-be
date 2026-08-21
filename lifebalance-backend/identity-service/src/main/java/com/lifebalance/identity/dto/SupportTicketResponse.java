package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.SupportTicketCategory;
import com.lifebalance.identity.model.enums.SupportTicketPriority;
import com.lifebalance.identity.model.enums.SupportTicketStatus;

import lombok.Builder;

@Builder
public record SupportTicketResponse(
        UUID id,
        String ticketNumber,
        UUID requesterId,
        String requesterEmail,
        UUID assigneeId,
        String assigneeEmail,
        String title,
        String description,
        SupportTicketStatus status,
        SupportTicketPriority priority,
        SupportTicketCategory category,
        String resolution,
        String escalationReason,
        OffsetDateTime receivedAt,
        OffsetDateTime assignedAt,
        OffsetDateTime resolvedAt,
        OffsetDateTime closedAt,
        OffsetDateTime reopenedAt,
        OffsetDateTime lastStatusChangedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
