package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.identity.config.MethodSecurityConfig;
import com.lifebalance.identity.config.SecurityConfig;
import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.dto.AssignRoleRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.security.CustomPermissionEvaluator;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.PermissionEvaluationService;
import com.lifebalance.identity.service.RolePermissionService;
import com.lifebalance.identity.service.UserRoleService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({
        AuditController.class,
        UserRoleController.class,
        RolePermissionController.class
})
@Import({
        SecurityConfig.class,
        MethodSecurityConfig.class,
        CustomPermissionEvaluator.class,
        LifebalanceMethodSecurityAutoConfiguration.class,
        IdentityControllerMethodSecurityIntegrationTest.TestSecuritySupport.class
})
class IdentityControllerMethodSecurityIntegrationTest {

    private static final UUID USER_ID = UUID.fromString("2529072d-2b08-4219-9105-a022f35d82e3");
    private static final UUID ROLE_ID = UUID.fromString("6572c24a-a93d-4e18-9245-c6583276018c");
    private static final UUID PERMISSION_ID = UUID.fromString("099cb8d2-9476-418a-9f6a-9dc2d972a943");
    private static final UUID ACTOR_ID = UUID.fromString("c2c9e2c2-9cf7-4b84-bf27-07269518661b");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private UserRoleService userRoleService;

    @MockitoBean
    private RolePermissionService rolePermissionService;

    @MockitoBean
    private KeycloakUserMappingService keycloakUserMappingService;

    @MockitoBean(name = "permissionEvaluationService")
    private PermissionEvaluationService permissionEvaluationService;

    @Test
    void shouldDenyAuditLogsWhenPermissionIsMissing() throws Exception {
        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("audit:read")))
                .thenReturn(false);

        mockMvc.perform(get("/audit-logs")
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-1"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(auditLogService, never()).getAll(any(Pageable.class));
    }

    @Test
    void shouldAllowAuditLogsWhenPermissionIsGranted() throws Exception {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.LOGIN);

        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("audit:read")))
                .thenReturn(true);
        when(auditLogService.getAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(log)));

        mockMvc.perform(get("/audit-logs")
                        .with(jwt().jwt(jwt -> jwt.subject("kc-admin-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"));

        verify(auditLogService).getAll(any(Pageable.class));
    }

    @Test
    void shouldDenyRoleAssignmentWhenPermissionIsMissing() throws Exception {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setRoleId(ROLE_ID);

        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("role:assign")))
                .thenReturn(false);

        mockMvc.perform(post("/users/{userId}/roles", USER_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(keycloakUserMappingService, never()).map(any());
        verify(userRoleService, never()).assignRole(any(), any(), any());
    }

    @Test
    void shouldAllowRoleAssignmentWhenPermissionIsGranted() throws Exception {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setRoleId(ROLE_ID);
        CurrentUser actor = mock(CurrentUser.class);

        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("role:assign")))
                .thenReturn(true);
        when(actor.getUserId()).thenReturn(ACTOR_ID.toString());
        when(keycloakUserMappingService.map(any())).thenReturn(actor);

        mockMvc.perform(post("/users/{userId}/roles", USER_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-admin-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userRoleService).assignRole(eq(USER_ID), any(AssignRoleRequest.class), eq(ACTOR_ID.toString()));
    }

    @Test
    void shouldAllowCurrentUserToReadOwnRolesWithoutRoleReadPermission() throws Exception {
        RoleResponse response = new RoleResponse();
        response.setId(ROLE_ID);

        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("role:read")))
                .thenReturn(false);
        when(permissionEvaluationService.isCurrentUser(any(Authentication.class), eq(USER_ID)))
                .thenReturn(true);
        when(userRoleService.getRoles(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/users/{userId}/roles", USER_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ROLE_ID.toString()));

        verify(userRoleService).getRoles(USER_ID);
    }

    @Test
    void shouldDenyPermissionAssignmentWhenPermissionIsMissing() throws Exception {
        AssignPermissionRequest request = new AssignPermissionRequest();
        request.setPermissionId(PERMISSION_ID);

        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("role:assign")))
                .thenReturn(false);

        mockMvc.perform(post("/roles/{roleId}/permissions", ROLE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-user-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.FORBIDDEN));

        verify(rolePermissionService, never()).assignPermission(any(), any());
    }

    @Test
    void shouldAllowPermissionReadWhenPermissionIsGranted() throws Exception {
        PermissionResponse response = new PermissionResponse();
        response.setId(PERMISSION_ID);

        when(permissionEvaluationService.hasPermission(any(Authentication.class), eq("permission:read")))
                .thenReturn(true);
        when(rolePermissionService.getPermissions(ROLE_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/roles/{roleId}/permissions", ROLE_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("kc-admin-1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PERMISSION_ID.toString()));

        verify(rolePermissionService).getPermissions(ROLE_ID);
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
