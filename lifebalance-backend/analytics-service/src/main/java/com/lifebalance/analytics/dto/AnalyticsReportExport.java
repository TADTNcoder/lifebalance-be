package com.lifebalance.analytics.dto;

public record AnalyticsReportExport(
        String filename,
        String contentType,
        byte[] content
) {
}
