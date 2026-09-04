package com.lifebalance.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class IdentityAccountStatusTokenValidatorTest {

    private static final URI VALIDATION_URL = URI.create(
            "http://identity-service:8080/api/internal/session/validate"
    );

    private MockRestServiceServer server;
    private IdentityAccountStatusTokenValidator validator;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        validator = new IdentityAccountStatusTokenValidator(builder.build(), VALIDATION_URL);
    }

    @Test
    void shouldAcceptTokenWhenIdentityConfirmsActiveSession() {
        server.expect(requestTo(VALIDATION_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        OAuth2TokenValidatorResult result = validator.validate(jwt());

        assertThat(result.hasErrors()).isFalse();
        server.verify();
    }

    @Test
    void shouldRejectTokenWhenIdentityReportsInactiveAccount() {
        server.expect(requestTo(VALIDATION_URL))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        OAuth2TokenValidatorResult result = validator.validate(jwt());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting(error -> error.getErrorCode())
                .containsExactly("invalid_token");
        server.verify();
    }

    @Test
    void shouldFailClosedWhenIdentityValidationIsUnavailable() {
        server.expect(requestTo(VALIDATION_URL))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        OAuth2TokenValidatorResult result = validator.validate(jwt());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .extracting(error -> error.getErrorCode())
                .containsExactly("temporarily_unavailable");
        server.verify();
    }

    private static Jwt jwt() {
        Instant issuedAt = Instant.parse("2026-09-05T00:00:00Z");
        return Jwt.withTokenValue("access-token")
                .header("alg", "RS256")
                .subject("kc-user-1")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(900))
                .build();
    }
}
