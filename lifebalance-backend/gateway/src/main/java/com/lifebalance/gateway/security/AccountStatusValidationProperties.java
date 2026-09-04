package com.lifebalance.gateway.security;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lifebalance.security.account-status-validation")
public class AccountStatusValidationProperties {

    private boolean enabled = true;
    private URI url = URI.create("http://identity-service:8080/api/internal/session/validate");

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }
}
