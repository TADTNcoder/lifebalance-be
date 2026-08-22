package com.lifebalance.identity.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import lombok.Builder;

@Builder
public record AdministrationReportResponse(
        String reportType,
        OffsetDateTime periodStart,
        OffsetDateTime periodEnd,
        OffsetDateTime generatedAt,
        Map<String, Long> metrics
) {
}
