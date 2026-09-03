package com.lifebalance.identity.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lifebalance.keycloak.password-change")
public class PasswordChangeProperties {

    private boolean enabled;
    private String serverUrl = "http://localhost:8088";
    private String realm = "lifebalance";
    private String verifierClientId = "lifebalance-password-verifier";
    private String verifierClientSecret;
    private String adminAuthRealm = "lifebalance";
    private String adminClientId = "lifebalance-password-admin";
    private String adminClientSecret;
    private int maximumAttempts = 5;
    private Duration attemptWindow = Duration.ofMinutes(15);
    private int minimumLength = 12;
    private int maximumLength = 128;

    public void validate() {
        require(serverUrl, "Keycloak password change server-url is required");
        require(realm, "Keycloak password change realm is required");
        require(verifierClientId, "Keycloak password verifier client-id is required");
        require(verifierClientSecret, "Keycloak password verifier client-secret is required");
        require(adminAuthRealm, "Keycloak password admin auth-realm is required");
        require(adminClientId, "Keycloak password admin client-id is required");
        require(adminClientSecret, "Keycloak password admin client-secret is required");

        if (maximumAttempts < 1) {
            throw new IllegalStateException("Password change maximum-attempts must be positive");
        }
        if (attemptWindow == null || attemptWindow.isZero() || attemptWindow.isNegative()) {
            throw new IllegalStateException("Password change attempt-window must be positive");
        }
        if (minimumLength < 1 || maximumLength < minimumLength) {
            throw new IllegalStateException("Password change length policy is invalid");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = trimToNull(serverUrl);
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = trimToNull(realm);
    }

    public String getVerifierClientId() {
        return verifierClientId;
    }

    public void setVerifierClientId(String verifierClientId) {
        this.verifierClientId = trimToNull(verifierClientId);
    }

    public String getVerifierClientSecret() {
        return verifierClientSecret;
    }

    public void setVerifierClientSecret(String verifierClientSecret) {
        this.verifierClientSecret = trimToNull(verifierClientSecret);
    }

    public String getAdminAuthRealm() {
        return adminAuthRealm;
    }

    public void setAdminAuthRealm(String adminAuthRealm) {
        this.adminAuthRealm = trimToNull(adminAuthRealm);
    }

    public String getAdminClientId() {
        return adminClientId;
    }

    public void setAdminClientId(String adminClientId) {
        this.adminClientId = trimToNull(adminClientId);
    }

    public String getAdminClientSecret() {
        return adminClientSecret;
    }

    public void setAdminClientSecret(String adminClientSecret) {
        this.adminClientSecret = trimToNull(adminClientSecret);
    }

    public int getMaximumAttempts() {
        return maximumAttempts;
    }

    public void setMaximumAttempts(int maximumAttempts) {
        this.maximumAttempts = maximumAttempts;
    }

    public Duration getAttemptWindow() {
        return attemptWindow;
    }

    public void setAttemptWindow(Duration attemptWindow) {
        this.attemptWindow = attemptWindow;
    }

    public int getMinimumLength() {
        return minimumLength;
    }

    public void setMinimumLength(int minimumLength) {
        this.minimumLength = minimumLength;
    }

    public int getMaximumLength() {
        return maximumLength;
    }

    public void setMaximumLength(int maximumLength) {
        this.maximumLength = maximumLength;
    }

    private static void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
