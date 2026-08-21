package com.lifebalance.analytics.domain;

import com.lifebalance.analytics.error.AnalyticsExceptions;
import java.util.Locale;

public enum ReportExportFormat {
    CSV("text/csv", "csv"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    PDF("application/pdf", "pdf");

    private final String contentType;
    private final String extension;

    ReportExportFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() {
        return contentType;
    }

    public String extension() {
        return extension;
    }

    public static ReportExportFormat from(String value) {
        if (value == null || value.isBlank()) {
            throw AnalyticsExceptions.invalidRequest("export format is required.");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if ("XLSX".equals(normalized)) {
            return EXCEL;
        }
        for (ReportExportFormat format : values()) {
            if (format.name().equals(normalized) || format.extension.equalsIgnoreCase(normalized)) {
                return format;
            }
        }
        throw AnalyticsExceptions.invalidRequest("export format must be one of CSV, EXCEL, XLSX, or PDF.");
    }
}
