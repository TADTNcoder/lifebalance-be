package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.identity.config.MethodSecurityConfig;
import com.lifebalance.identity.config.SecurityConfig;
import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.security.CustomPermissionEvaluator;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc security scenarios mapped to TC_PERM_14 .. TC_PERM_19 and TC_PERM_21.
 *
 * <p>Không cần Postman. SecurityFilterChain + method security thật được bật;
 * PermissionEvaluationService và service nghiệp vụ được mock để tập trung test
 * authorization/HTTP contract.
 */
@WebMvcTest(UserController.class)
@Import({
        SecurityConfig.class,
        MethodSecurityConfig.class,
        CustomPermissionEvaluator.class,
        LifebalanceMethodSecurityAutoConfiguration.class,
        UserAdministrationSecurityMockMvcTest.TestSecuritySupport.class
})
class UserAdministrationSecurityMockMvcTest {

    private static final UUID USER_A_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_B_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

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
    @DisplayName("TC_PERM_14 - Admin có user:update được disable tài khoản")
    void tcPerm14_adminCanDisableUser() throws Exception {
        when(permissionEvaluationService.hasPermission(
                any(Authentication.class), eq("user:update")))
                .thenReturn(true);
        when(userService.disableUser(USER_B_ID))
                .thenReturn(userResponse(USER_B_ID, AccountStatus.DISABLED));

        mockMvc.perform(patch("/api/users/{id}/disable", USER_B_ID)
                        .with(jwt().jwt(token -> token.subject("kc-admin-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_B_ID.toString()))
                .andExpect(jsonPath("$.status").value("DISABLED"));

        verify(userService).disableUser(USER_B_ID);
    }

    @Test
    @DisplayName("TC_PERM_15 - User thường không có user:update bị chặn 403")
    void tcPerm15_normalUserCannotDisableOtherUser() throws Exception {
        when(permissionEvaluationService.hasPermission(
                any(Authentication.class), eq("user:update")))
                .thenReturn(false);

        mockMvc.perform(patch("/api/users/{id}/disable", USER_B_ID)
                        .with(jwt().jwt(token -> token.subject("kc-user-a"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(userService, never()).disableUser(any(UUID.class));
    }

    @Test
    @DisplayName("TC_PERM_16 - Admin có user:delete được soft delete tài khoản")
    void tcPerm16_adminCanSoftDeleteUser() throws Exception {
        when(permissionEvaluationService.hasPermission(
                any(Authentication.class), eq("user:delete")))
                .thenReturn(true);

        mockMvc.perform(delete("/api/users/{id}", USER_B_ID)
                        .with(jwt().jwt(token -> token.subject("kc-admin-1"))))
                .andExpect(status().isNoContent());

        verify(userService).softDeleteUser(USER_B_ID);
    }

    @Test
    @DisplayName("TC_PERM_17 - User thường không có user:delete bị chặn 403")
    void tcPerm17_normalUserCannotDeleteOtherUser() throws Exception {
        when(permissionEvaluationService.hasPermission(
                any(Authentication.class), eq("user:delete")))
                .thenReturn(false);

        mockMvc.perform(delete("/api/users/{id}", USER_B_ID)
                        .with(jwt().jwt(token -> token.subject("kc-user-a"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(userService, never()).softDeleteUser(any(UUID.class));
    }

    @Test
    @DisplayName("TC_PERM_18 - User cập nhật hồ sơ của chính mình qua /api/users/me")
    void tcPerm18_userCanUpdateOwnProfile() throws Exception {
        CurrentUser currentUser = new CurrentUser(
                USER_A_ID.toString(),
                "user-a",
                "user-a@example.com",
                List.of("user"));
        User updated = userEntity(USER_A_ID, AccountStatus.ACTIVE);
        updated.setDisplayName("User A Updated");
        updated.setEmail("user-a.updated@example.com");

        when(keycloakUserMappingService.map(any(Jwt.class))).thenReturn(currentUser);
        when(internalUserService.updateCurrentUser(
                eq(currentUser), any(UpdateUserRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .with(jwt().jwt(token -> token.subject("kc-user-a")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "User A Updated",
                                  "email": "user-a.updated@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_A_ID.toString()))
                .andExpect(jsonPath("$.displayName").value("User A Updated"))
                .andExpect(jsonPath("$.email").value("user-a.updated@example.com"));

        verify(internalUserService)
                .updateCurrentUser(eq(currentUser), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("TC_PERM_19 - User A không được PATCH hồ sơ User B")
    void tcPerm19_userCannotUpdateOtherUser() throws Exception {
        when(permissionEvaluationService.hasPermission(
                any(Authentication.class), eq("user"), eq("update")))
                .thenReturn(false);

        mockMvc.perform(patch("/api/users/{id}", USER_B_ID)
                        .with(jwt().jwt(token -> token.subject("kc-user-a")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Hacked name"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(userService, never())
                .updateUser(any(UUID.class), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("TC_PERM_21 - API /api/users thiếu JWT trả về 401")
    void tcPerm21_protectedUserApiWithoutJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/disable", USER_B_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(userService, never()).disableUser(any(UUID.class));
    }

    private static UserResponse userResponse(UUID id, AccountStatus status) {
        UserResponse response = new UserResponse();
        response.setId(id);
        response.setEmail("user@example.com");
        response.setUsername("user");
        response.setDisplayName("LifeBalance User");
        response.setStatus(status);
        response.setRegisteredAt(OffsetDateTime.parse("2026-08-01T08:00:00+07:00"));
        response.setLastLoginAt(OffsetDateTime.parse("2026-08-31T08:00:00+07:00"));
        return response;
    }

    private static User userEntity(UUID id, AccountStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail("user-a.updated@example.com");
        user.setUsername("user-a");
        user.setDisplayName("User A Updated");
        user.setStatus(status);
        user.setRegisteredAt(OffsetDateTime.parse("2026-08-01T08:00:00+07:00"));
        user.setLastLoginAt(OffsetDateTime.parse("2026-08-31T08:00:00+07:00"));
        return user;
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
            return new LifebalanceAuthenticationEntryPoint(
                    objectMapper,
                    authenticationFailureLogger);
        }

        @Bean
        LifebalanceAccessDeniedHandler lifebalanceAccessDeniedHandler(
                ObjectMapper objectMapper,
                AuthorizationFailureLogger authorizationFailureLogger
        ) {
            return new LifebalanceAccessDeniedHandler(
                    objectMapper,
                    authorizationFailureLogger);
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new JwtException("JWT decoding is not used by MockMvc jwt() tests");
            };
        }
    }
}
