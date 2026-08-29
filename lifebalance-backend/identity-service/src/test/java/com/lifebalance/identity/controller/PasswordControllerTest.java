package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.lifebalance.common.error.GlobalExceptionHandler;
import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.error.PasswordChangeExceptions;
import com.lifebalance.identity.model.User;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.model.enums.AuditStatus;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.PasswordChangeService;

@ExtendWith(MockitoExtension.class)
class PasswordControllerTest {

    @Mock
    private InternalUserService internalUserService;

    @Mock
    private KeycloakUserMappingService keycloakUserMappingService;

    @Mock
    private PasswordChangeService passwordChangeService;

    @Mock
    private AuditLogService auditLogService;

    private MockMvc mockMvc;
    private CurrentUser currentUser;
    private User user;

    @BeforeEach
    void setUp() {
        PasswordController controller = new PasswordController(
                internalUserService,
                keycloakUserMappingService,
                passwordChangeService,
                auditLogService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new JwtArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        currentUser = new CurrentUser("kc-user-1", "alice", "alice@example.com", List.of("user"));
        user = new User();
        user.setId(UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031"));
        user.setKeycloakId("kc-user-1");
        user.setUsername("alice");
    }

    @Test
    void shouldChangePasswordWithoutReturningCredentialsAndWriteSuccessAudit() throws Exception {
        when(keycloakUserMappingService.map(any(Jwt.class))).thenReturn(currentUser);
        when(internalUserService.findOrCreate(currentUser)).thenReturn(user);
        when(passwordChangeService.changePassword(eq(user), eq("alice"), any(), eq("127.0.0.1")))
                .thenReturn(new PasswordChangeService.Result(true));

        mockMvc.perform(put("/users/me/password")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header(HttpHeaders.USER_AGENT, "JUnit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.reauthenticationRequired").value(true))
                .andExpect(jsonPath("$.sessionsRevoked").value(true))
                .andExpect(jsonPath("$.currentPassword").doesNotExist())
                .andExpect(jsonPath("$.newPassword").doesNotExist())
                .andExpect(jsonPath("$.confirmPassword").doesNotExist());

        verify(auditLogService).saveAudit(
                user,
                AuditAction.CHANGE_PASSWORD,
                AuditStatus.SUCCESS,
                "127.0.0.1",
                "JUnit",
                "Password changed; reauthentication required"
        );
    }

    @Test
    void shouldReturnSafeErrorAndWriteFailureAuditForIncorrectCurrentPassword() throws Exception {
        when(keycloakUserMappingService.map(any(Jwt.class))).thenReturn(currentUser);
        when(internalUserService.findOrCreate(currentUser)).thenReturn(user);
        when(passwordChangeService.changePassword(eq(user), eq("alice"), any(), eq("127.0.0.1")))
                .thenThrow(PasswordChangeExceptions.invalidCurrentPassword());

        mockMvc.perform(put("/api/users/me/password")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .header(HttpHeaders.USER_AGENT, "JUnit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(IdentityErrorCode.CURRENT_PASSWORD_INVALID))
                .andExpect(jsonPath("$.error.details.currentPassword").isNotEmpty())
                .andExpect(jsonPath("$.error.message").value("Current password is incorrect"))
                .andExpect(jsonPath("$.error.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("OldPassword1!"))));

        verify(auditLogService).saveAudit(
                user,
                AuditAction.CHANGE_PASSWORD,
                AuditStatus.FAILED,
                "127.0.0.1",
                "JUnit",
                "Password change failed (" + IdentityErrorCode.CURRENT_PASSWORD_INVALID + ')'
        );
    }

    @Test
    void shouldValidateRequestBeforeCallingPasswordService() throws Exception {
        mockMvc.perform(put("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "",
                                  "newPassword": "short",
                                  "confirmPassword": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.details.currentPassword").isNotEmpty())
                .andExpect(jsonPath("$.error.details.newPassword").isNotEmpty());

        verify(passwordChangeService, never()).changePassword(any(), any(), any(), any());
    }

    private static String validRequest() {
        return """
                {
                  "currentPassword": "OldPassword1!",
                  "newPassword": "NewPassword1!",
                  "confirmPassword": "NewPassword1!"
                }
                """;
    }

    private static final class JwtArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && Jwt.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .claim("sub", "kc-user-1")
                    .build();
        }
    }
}
