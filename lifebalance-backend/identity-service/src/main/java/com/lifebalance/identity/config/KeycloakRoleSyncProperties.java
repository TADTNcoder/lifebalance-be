package com.lifebalance.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lifebalance.keycloak.role-sync")
public class KeycloakRoleSyncProperties {

    private boolean enabled;
    private String serverUrl = "http://localhost:8082";
    private String realm = "lifebalance";
    private String authRealm = "master";
    private String clientId = "admin-cli";
    private String clientSecret;
    private String username;
    private String password;

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

    public String getAuthRealm() {
        return authRealm;
    }

    public void setAuthRealm(String authRealm) {
        this.authRealm = trimToNull(authRealm);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = trimToNull(clientId);
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = trimToNull(clientSecret);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = trimToNull(username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = trimToNull(password);
    }

    public void validate() {
        require(serverUrl, "Keycloak role sync server-url is required");
        require(realm, "Keycloak role sync realm is required");
        require(authRealm, "Keycloak role sync auth-realm is required");
        require(clientId, "Keycloak role sync client-id is required");

        if (hasText(clientSecret)) {
            return;
        }

        require(username, "Keycloak role sync username is required when client-secret is not configured");
        require(password, "Keycloak role sync password is required when client-secret is not configured");
    }

    private static void require(String value, String message) {
        if (!hasText(value)) {
            throw new IllegalStateException(message);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
