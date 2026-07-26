package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.identity.config.SecurityConfig;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.PermissionEvaluationService;
import com.lifebalance.identity.service.UserService;
import com.lifebalance.security.keycloak.AuthenticationFailureLogger;
import com.lifebalance.security.keycloak.AuthorizationFailureLogger;
import com.lifebalance.security.keycloak.LifebalanceAccessDeniedHandler;
import com.lifebalance.security.keycloak.LifebalanceAuthenticationEntryPoint;
import com.lifebalance.security.method.LifebalanceMethodSecurityAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        LifebalanceMethodSecurityAutoConfiguration.class,
        UserControllerMethodSecurityIntegrationTest.TestSecuritySupport.class
})
class UserControllerMethodSecurityIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InternalUserService internalUserService;

    @MockitoBean
    private KeycloakUserMappingService keycloakUserMappingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean(name = "permissionEvaluationService")
    private PermissionEvaluationService permissionEvaluationService;

    @Test
    void shouldAllowAuthenticatedUserWithRequiredPermission() throws Exception {
        UserResponse response = createUserResponse(USER_ID);

        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("user:read")))
                .thenReturn(true);
        when(userService.getUserById(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", USER_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-admin-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(userService).getUserById(USER_ID);
    }

    @Test
    void shouldReturnForbiddenWhenAuthenticatedUserLacksRequiredPermission() throws Exception {
        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("user:read")))
                .thenReturn(false);
        when(permissionEvaluationService.isCurrentUser(any(Authentication.class), eq(USER_ID)))
                .thenReturn(false);

        mockMvc.perform(get("/users/{id}", USER_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-1"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN))
                .andExpect(jsonPath("$.error.message").value("Access is denied"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verify(userService, never()).getUserById(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/users/{id}", USER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED))
                .andExpect(jsonPath("$.error.message").value("Authentication is required"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());

        verify(permissionEvaluationService, never())
                .hasPermission(any(Authentication.class), any(String.class));
        verify(userService, never()).getUserById(any());
    }

    private static UserResponse createUserResponse(UUID userId) {
        UserResponse response = new UserResponse();
        response.setId(userId);
        response.setEmail("alice@example.com");
        response.setUsername("alice");
        response.setDisplayName("Alice Nguyen");
        response.setStatus(AccountStatus.ACTIVE);
        response.setRegisteredAt(OffsetDateTime.parse("2026-07-20T10:15:30Z"));
        response.setLastLoginAt(OffsetDateTime.parse("2026-07-21T11:20:30Z"));

        return response;
    }

    @TestConfiguration
    static class TestSecuritySupport {

        @Bean
        AuthenticationFailureLogger authenticationFailureLogger() {
            return new AuthenticationFailureLogger();
        }

        @Bean
        AuthorizationFailureLogger authorizationFailureLogger() {
            return new AuthorizationFailureLogger();
        }

        @Bean
        LifebalanceAuthenticationEntryPoint lifebalanceAuthenticationEntryPoint(
                ObjectMapper objectMapper,
                AuthenticationFailureLogger authenticationFailureLogger
        ) {
            return new LifebalanceAuthenticationEntryPoint(objectMapper, authenticationFailureLogger);
        }

        @Bean
        LifebalanceAccessDeniedHandler lifebalanceAccessDeniedHandler(
                ObjectMapper objectMapper,
                AuthorizationFailureLogger authorizationFailureLogger
        ) {
            return new LifebalanceAccessDeniedHandler(objectMapper, authorizationFailureLogger);
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new JwtException("JWT decoding is not used by this test");
            };
        }
    }
}
