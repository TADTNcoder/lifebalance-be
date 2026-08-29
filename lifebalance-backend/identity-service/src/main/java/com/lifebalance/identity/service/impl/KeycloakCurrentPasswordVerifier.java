package com.lifebalance.identity.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.error.PasswordChangeExceptions;
import com.lifebalance.identity.service.CurrentPasswordVerifier;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KeycloakCurrentPasswordVerifier implements CurrentPasswordVerifier {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final PasswordChangeProperties properties;

    public KeycloakCurrentPasswordVerifier(
            RestClient restClient,
            ObjectMapper objectMapper,
            PasswordChangeProperties properties
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public boolean verify(String username, String currentPassword) {
        MultiValueMap<String, String> form = clientForm();
        form.add("grant_type", "password");
        form.add("username", username);
        form.add("password", currentPassword);

        try {
            TokenResponse token = restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", properties.getRealm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);

            if (token == null || token.refreshToken() == null || token.refreshToken().isBlank()) {
                throw PasswordChangeExceptions.unavailable();
            }

            revokeVerificationSession(token.refreshToken());
            return true;
        } catch (RestClientResponseException exception) {
            if (isInvalidGrant(exception)) {
                return false;
            }
            throw PasswordChangeExceptions.unavailable();
        } catch (RestClientException exception) {
            throw PasswordChangeExceptions.unavailable();
        }
    }

    private void revokeVerificationSession(String refreshToken) {
        MultiValueMap<String, String> form = clientForm();
        form.add("refresh_token", refreshToken);

        try {
            restClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/logout", properties.getRealm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            log.warn("Keycloak did not revoke the temporary password-verification session");
        }
    }

    private MultiValueMap<String, String> clientForm() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getVerifierClientId());
        form.add("client_secret", properties.getVerifierClientSecret());
        return form;
    }

    private boolean isInvalidGrant(RestClientResponseException exception) {
        if (exception.getStatusCode().value() != 400) {
            return false;
        }

        try {
            JsonNode body = objectMapper.readTree(exception.getResponseBodyAsByteArray());
            return "invalid_grant".equals(body.path("error").asText());
        } catch (Exception ignored) {
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(@JsonProperty("refresh_token") String refreshToken) {
    }
}
