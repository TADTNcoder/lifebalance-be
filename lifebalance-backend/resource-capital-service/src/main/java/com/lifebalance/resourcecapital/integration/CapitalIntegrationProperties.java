package com.lifebalance.resourcecapital.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lifebalance.integration")
public class CapitalIntegrationProperties {

    private boolean enabled = true;

    private NotificationEndpoint notificationService = new NotificationEndpoint();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public NotificationEndpoint getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationEndpoint notificationService) {
        this.notificationService = notificationService == null
                ? new NotificationEndpoint()
                : notificationService;
    }

    public boolean isNotificationSyncEnabled() {
        return enabled
                && notificationService != null
                && notificationService.isEnabled()
                && notificationService.isPolicyApproved();
    }

    public static class NotificationEndpoint {

        private String baseUrl = "http://notification-service:8080";

        private boolean enabled = true;

        private boolean policyApproved = false;

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

        public boolean isPolicyApproved() {
            return policyApproved;
        }

        public void setPolicyApproved(boolean policyApproved) {
            this.policyApproved = policyApproved;
        }
    }
}
