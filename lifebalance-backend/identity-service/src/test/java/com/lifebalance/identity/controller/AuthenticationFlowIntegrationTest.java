package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc // VẪN BẬT FULL BẢO MẬT
class AuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock bộ giải mã Token để không cần kết nối tới Keycloak thật
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectUnauthenticatedRequestWith401Unauthorized() throws Exception {
        // Kịch bản 1: Không truyền Token -> Bị đá 401
        mockMvc.perform(get("/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAcceptAuthenticatedStatusRequestWithValidJwtToken() throws Exception {
        // Tạo một object JWT giả lập hợp lệ
        Jwt mockJwt = new Jwt("fake-token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"), Map.of(
                "sub", "kc-user-1",
                "preferred_username", "testuser",
                "email", "testuser@example.com"
        ));

        // Khi hệ thống đòi giải mã, trả về cái token giả này
        when(jwtDecoder.decode(anyString())).thenReturn(mockJwt);

        // Kịch bản 2: Có truyền Header Authorization -> Đi qua màng lọc, trả về 200 OK
        mockMvc.perform(get("/api/v1/identity/status")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowIdentityStatusEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/identity/status"))
                .andExpect(status().isOk());

        Jwt mockJwt = new Jwt("fake-token", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"), Map.of(
                "sub", "kc-admin-1",
                "preferred_username", "admin",
                "email", "admin@example.com"
        ));
        when(jwtDecoder.decode(anyString())).thenReturn(mockJwt);

        mockMvc.perform(get("/api/v1/identity/status")
                        .header("Authorization", "Bearer fake-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowIdentityReadinessEndpointsWithInvalidAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/v1/identity/status")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/identity/health")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isOk());

        verify(jwtDecoder, never()).decode("expired-token");
    }
}
