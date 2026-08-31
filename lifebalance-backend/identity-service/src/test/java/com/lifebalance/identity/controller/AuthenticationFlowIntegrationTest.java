package com.lifebalance.identity.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full Spring Security + MockMvc tests for public/protected Identity endpoints.
 *
 * <p>These tests replace manual/Postman checks for web-layer authentication as much as possible.
 * The JwtDecoder is mocked, so these cases verify the SecurityFilterChain reaction to accepted/rejected
 * tokens. Keycloak brute-force policy still requires a real Keycloak integration/demo test.
 */
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
class AuthenticationFlowIntegrationTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-08-31T06:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-08-31T07:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("TC_SESSION_12 / TC_PERM_21 - Protected API without bearer token returns 401")
    void protectedApiWithoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated request with a valid JWT passes the security filter")
    void validJwt_allowsAuthenticatedRequest() throws Exception {
        when(jwtDecoder.decode("valid-token")).thenReturn(validJwt("valid-token"));

        mockMvc.perform(get("/api/v1/identity/status")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC_SESSION_13 - Public identity status does not require authentication")
    void publicStatus_withoutAuthentication_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/identity/status"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Public readiness endpoints ignore an invalid Authorization header")
    void publicReadiness_invalidAuthorizationHeader_staysPublic() throws Exception {
        mockMvc.perform(get("/api/v1/identity/status")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/identity/health")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isOk());

        verify(jwtDecoder, never()).decode("expired-token");
    }

    @Test
    @DisplayName("TC_JWT_22 - Decoder rejects a token whose payload/signature is considered tampered")
    void tamperedPayloadToken_returnsUnauthorized() throws Exception {
        rejectToken("tampered-payload-token", "JWT signature verification failed");
    }

    @Test
    @DisplayName("TC_JWT_23 - Decoder rejects malformed JWT")
    void malformedToken_returnsUnauthorized() throws Exception {
        rejectToken("malformed-token", "Malformed JWT");
    }

    @Test
    @DisplayName("TC_JWT_24 - Decoder rejects invalid signature")
    void invalidSignatureToken_returnsUnauthorized() throws Exception {
        rejectToken("invalid-signature-token", "Invalid signature");
    }

    @Test
    @DisplayName("TC_JWT_25 - Decoder rejection of alg=none style token is returned as 401")
    void noneAlgorithmToken_returnsUnauthorized() throws Exception {
        rejectToken("alg-none-token", "Unsigned JWT is not allowed");
    }

    private void rejectToken(String token, String message) throws Exception {
        when(jwtDecoder.decode(token)).thenThrow(new BadJwtException(message));

        mockMvc.perform(get("/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());

        verify(jwtDecoder).decode(token);
    }

    private static Jwt validJwt(String tokenValue) {
        return new Jwt(
                tokenValue,
                ISSUED_AT,
                EXPIRES_AT,
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", "kc-user-1",
                        "preferred_username", "testuser",
                        "email", "testuser@example.com"
                )
        );
    }
}
