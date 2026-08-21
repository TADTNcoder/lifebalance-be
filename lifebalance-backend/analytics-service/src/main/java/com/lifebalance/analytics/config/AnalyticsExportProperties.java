package com.lifebalance.analytics.config;

import com.lifebalance.analytics.domain.ReportExportFormat;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lifebalance.analytics.export")
public class AnalyticsExportProperties {

    private boolean enabled = true;

    private boolean auditEnabled = true;

    private Set<ReportExportFormat> allowedFormats = EnumSet.allOf(ReportExportFormat.class);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public Set<ReportExportFormat> getAllowedFormats() {
        return allowedFormats;
    }

    public void setAllowedFormats(Set<ReportExportFormat> allowedFormats) {
        this.allowedFormats = allowedFormats == null || allowedFormats.isEmpty()
                ? EnumSet.noneOf(ReportExportFormat.class)
                : EnumSet.copyOf(allowedFormats);
    }

    public boolean isAllowed(ReportExportFormat format) {
        return enabled && format != null && allowedFormats.contains(format);
    }
}
