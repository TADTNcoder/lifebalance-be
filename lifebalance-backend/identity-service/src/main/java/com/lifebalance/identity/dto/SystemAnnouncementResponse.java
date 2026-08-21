package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.identity.model.enums.AnnouncementAudience;
import com.lifebalance.identity.model.enums.AnnouncementStatus;

import lombok.Builder;

@Builder
public record SystemAnnouncementResponse(
        UUID id,
        String title,
        String message,
        AnnouncementAudience audience,
        AnnouncementStatus status,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        OffsetDateTime publishedAt,
        UUID publishedBy,
        OffsetDateTime cancelledAt,
        String cancellationReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
