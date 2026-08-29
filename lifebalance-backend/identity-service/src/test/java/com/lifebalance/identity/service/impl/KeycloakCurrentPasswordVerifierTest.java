package com.lifebalance.identity.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.error.AppException;
import com.lifebalance.identity.config.PasswordChangeProperties;
import com.lifebalance.identity.error.IdentityErrorCode;

class KeycloakCurrentPasswordVerifierTest {

    private MockRestServiceServer server;
    private KeycloakCurrentPasswordVerifier verifier;

    @BeforeEach
    void setUp() {
        PasswordChangeProperties properties = new PasswordChangeProperties();
        properties.setRealm("lifebalance");
        properties.setVerifierClientId("password-verifier");
        properties.setVerifierClientSecret("verifier-secret");

        RestClient.Builder builder = RestClient.builder().baseUrl("http://keycloak:8080");
        server = MockRestServiceServer.bindTo(builder).build();
        verifier = new KeycloakCurrentPasswordVerifier(builder.build(), new ObjectMapper(), properties);
    }

    @Test
    void shouldVerifyPasswordThenRevokeTemporaryVerificationSession() {
        server.expect(requestTo("http://keycloak:8080/realms/lifebalance/protocol/openid-connect/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(allOf(
                        containsString("grant_type=password"),
                        containsString("username=alice"),
                        containsString("password=CurrentPassword1%21"),
                        containsString("client_secret=verifier-secret")
                )))
                .andRespond(withSuccess("{\"refresh_token\":\"temporary-refresh-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://keycloak:8080/realms/lifebalance/protocol/openid-connect/logout"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(allOf(
                        containsString("refresh_token=temporary-refresh-token"),
                        containsString("client_secret=verifier-secret")
                )))
                .andRespond(withNoContent());

        assertThat(verifier.verify("alice", "CurrentPassword1!")).isTrue();
        server.verify();
    }

    @Test
    void shouldReturnFalseOnlyForKeycloakInvalidGrant() {
        server.expect(requestTo("http://keycloak:8080/realms/lifebalance/protocol/openid-connect/token"))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\"}"));

        assertThat(verifier.verify("alice", "wrong-password")).isFalse();
        server.verify();
    }

    @Test
    void shouldTreatVerifierClientMisconfigurationAsUnavailable() {
        server.expect(requestTo("http://keycloak:8080/realms/lifebalance/protocol/openid-connect/token"))
                .andRespond(withBadRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"unauthorized_client\"}"));

        assertThatThrownBy(() -> verifier.verify("alice", "CurrentPassword1!"))
                .isInstanceOfSatisfying(AppException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(IdentityErrorCode.PASSWORD_CHANGE_UNAVAILABLE));
        server.verify();
    }
}
