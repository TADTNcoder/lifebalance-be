package com.lifebalance.security.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lifebalance.security.keycloak")
public class KeycloakSecurityProperties {

    private String clientId = "lifebalance-api";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            this.clientId = "lifebalance-api";
            return;
        }

        this.clientId = clientId;
    }

}
