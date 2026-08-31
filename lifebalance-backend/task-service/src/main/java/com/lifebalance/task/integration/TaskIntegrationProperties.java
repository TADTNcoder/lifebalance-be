package com.lifebalance.task.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lifebalance.integration")
public class TaskIntegrationProperties {

    private boolean enabled = true;

    private String internalSecret;

    private int maxAttempts = 3;

    private long retryBackoffMillis = 200;

    private long connectTimeoutMillis = 3_000;

    private long readTimeoutMillis = 5_000;

    private ServiceEndpoint timelineService = new ServiceEndpoint("http://timeline-service:8080", true);

    private NotificationEndpoint notificationService = new NotificationEndpoint();

    private AnalyticsEndpoint analyticsService = new AnalyticsEndpoint();

    private ServiceEndpoint financeService = new ServiceEndpoint("http://finance-service:8080", true);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getInternalSecret() {
        return internalSecret;
    }

    public void setInternalSecret(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    public boolean hasInternalSecret() {
        return internalSecret != null && !internalSecret.isBlank();
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    public long getRetryBackoffMillis() {
        return retryBackoffMillis;
    }

    public void setRetryBackoffMillis(long retryBackoffMillis) {
        this.retryBackoffMillis = Math.max(0, retryBackoffMillis);
    }

    public long getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(long connectTimeoutMillis) {
        this.connectTimeoutMillis = Math.max(1, connectTimeoutMillis);
    }

    public long getReadTimeoutMillis() {
        return readTimeoutMillis;
    }

    public void setReadTimeoutMillis(long readTimeoutMillis) {
        this.readTimeoutMillis = Math.max(1, readTimeoutMillis);
    }

    public ServiceEndpoint getTimelineService() {
        return timelineService;
    }

    public void setTimelineService(ServiceEndpoint timelineService) {
        this.timelineService = timelineService == null
                ? new ServiceEndpoint("http://timeline-service:8080", true)
                : timelineService;
    }

    public NotificationEndpoint getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationEndpoint notificationService) {
        this.notificationService = notificationService == null
                ? new NotificationEndpoint()
                : notificationService;
    }

    public AnalyticsEndpoint getAnalyticsService() {
        return analyticsService;
    }

    public void setAnalyticsService(AnalyticsEndpoint analyticsService) {
        this.analyticsService = analyticsService == null
                ? new AnalyticsEndpoint()
                : analyticsService;
    }

    public ServiceEndpoint getFinanceService() {
        return financeService;
    }

    public void setFinanceService(ServiceEndpoint financeService) {
        this.financeService = financeService == null
                ? new ServiceEndpoint("http://finance-service:8080", true)
                : financeService;
    }

    public boolean isTimelineSyncEnabled() {
        return enabled && timelineService != null && timelineService.isEnabled();
    }

    public boolean isNotificationSyncEnabled() {
        return enabled
                && notificationService != null
                && notificationService.isEnabled()
                && notificationService.isPolicyApproved();
    }

    public boolean isAnalyticsActualSeedEnabled() {
        return enabled
                && analyticsService != null
                && analyticsService.isEnabled()
                && analyticsService.isActualSeedEnabled();
    }

    public boolean isFinanceIncomeSyncEnabled() {
        return enabled && financeService != null && financeService.isEnabled();
    }

    public static class ServiceEndpoint {

        private String baseUrl;

        private boolean enabled;

        public ServiceEndpoint() {
            this(null, true);
        }

        public ServiceEndpoint(String baseUrl, boolean enabled) {
            this.baseUrl = baseUrl;
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class NotificationEndpoint extends ServiceEndpoint {

        private boolean policyApproved = false;

        public NotificationEndpoint() {
            super("http://notification-service:8080", true);
        }

        public boolean isPolicyApproved() {
            return policyApproved;
        }

        public void setPolicyApproved(boolean policyApproved) {
            this.policyApproved = policyApproved;
        }
    }

    public static class AnalyticsEndpoint extends ServiceEndpoint {

        private boolean actualSeedEnabled = false;

        private String defaultCurrencyCode = "VND";

        public AnalyticsEndpoint() {
            super("http://analytics-service:8080", true);
        }

        public boolean isActualSeedEnabled() {
            return actualSeedEnabled;
        }

        public void setActualSeedEnabled(boolean actualSeedEnabled) {
            this.actualSeedEnabled = actualSeedEnabled;
        }

        public String getDefaultCurrencyCode() {
            return defaultCurrencyCode;
        }

        public void setDefaultCurrencyCode(String defaultCurrencyCode) {
            this.defaultCurrencyCode = defaultCurrencyCode;
        }
    }
}
