package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.common.error.GlobalExceptionHandler;
import com.lifebalance.identity.dto.UpdateUserRequest;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.exception.UserAlreadyDeletedException;
import com.lifebalance.identity.exception.UserAlreadyDisabledException;
import com.lifebalance.identity.exception.UserEmailAlreadyExistsException;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.exception.UserUsernameAlreadyExistsException;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.service.AuditLogService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private InternalUserService internalUserService;

    @Mock
    private KeycloakUserMappingService keycloakUserMappingService;

    @Mock
    private UserService userService;

    @Mock
    private AuditLogService auditLogService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(
                internalUserService,
                keycloakUserMappingService,
                userService,
                auditLogService
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnUserById() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");
        UserResponse response = createUserResponse(userId);

        when(userService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.displayName").value("Alice Nguyen"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.registeredAt").isNotEmpty())
                .andExpect(jsonPath("$.lastLoginAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.keycloakId").doesNotExist());
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        UUID userId = UUID.fromString("70870326-4447-4ef6-a909-2c8dcfd81ba7");

        when(userService.getUserById(userId))
                .thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_NOT_FOUND))
                .andExpect(jsonPath("$.error.message")
                        .value("User not found: " + userId))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void shouldReturn400WhenUserIdIsNotUuid() throws Exception {
        mockMvc.perform(get("/users/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.message")
                        .value("Request parameter has invalid format"))
                .andExpect(jsonPath("$.error.details.id")
                        .value("must be a valid UUID"));

        verify(userService, never()).getUserById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldPatchUserById() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");
        UserResponse response = createUserResponse(userId);
        response.setEmail("alice.updated@example.com");
        response.setUsername("alice-updated");
        response.setDisplayName("Alice Updated");

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "Alice.Updated@Example.COM",
                                  "username": "Alice-Updated",
                                  "displayName": "Alice Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("alice.updated@example.com"))
                .andExpect(jsonPath("$.username").value("alice-updated"))
                .andExpect(jsonPath("$.displayName").value("Alice Updated"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.keycloakId").doesNotExist());

        ArgumentCaptor<UpdateUserRequest> requestCaptor = ArgumentCaptor.forClass(UpdateUserRequest.class);
        verify(userService).updateUser(eq(userId), requestCaptor.capture());
        UpdateUserRequest request = requestCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(request.getEmail()).isEqualTo("Alice.Updated@Example.COM");
        org.assertj.core.api.Assertions.assertThat(request.getUsername()).isEqualTo("Alice-Updated");
        org.assertj.core.api.Assertions.assertThat(request.getDisplayName()).isEqualTo("Alice Updated");
    }

    @Test
    void shouldReturn400WhenPatchUserIdIsNotUuid() throws Exception {
        mockMvc.perform(patch("/users/{id}", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Alice Updated"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.message")
                        .value("Request parameter has invalid format"))
                .andExpect(jsonPath("$.error.details.id")
                        .value("must be a valid UUID"));

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void shouldReturn400WhenPatchRequestValidationFails() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.error.details.email").isNotEmpty());

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void shouldReturn404WhenPatchTargetDoesNotExist() throws Exception {
        UUID userId = UUID.fromString("70870326-4447-4ef6-a909-2c8dcfd81ba7");

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "Alice Updated"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_NOT_FOUND))
                .andExpect(jsonPath("$.error.message")
                        .value("User not found: " + userId));
    }

    @Test
    void shouldReturn409WhenPatchEmailAlreadyExists() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new UserEmailAlreadyExistsException("taken@example.com"));

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "taken@example.com"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_EMAIL_ALREADY_EXISTS))
                .andExpect(jsonPath("$.error.message")
                        .value("Email already exists: taken@example.com"));
    }

    @Test
    void shouldReturn409WhenPatchUsernameAlreadyExists() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

        when(userService.updateUser(eq(userId), any(UpdateUserRequest.class)))
                .thenThrow(new UserUsernameAlreadyExistsException("taken"));

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "taken"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_USERNAME_ALREADY_EXISTS))
                .andExpect(jsonPath("$.error.message")
                        .value("Username already exists: taken"));
    }

    @Test
    void shouldDisableUserById() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");
        UserResponse response = createUserResponse(userId);
        response.setStatus(AccountStatus.DISABLED);

        when(userService.disableUser(userId)).thenReturn(response);

        mockMvc.perform(patch("/users/{id}/disable", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("DISABLED"));

        verify(userService).disableUser(userId);
    }

    @Test
    void shouldReturn409WhenUserIsAlreadyDisabled() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

        when(userService.disableUser(userId))
                .thenThrow(new UserAlreadyDisabledException(userId));

        mockMvc.perform(patch("/users/{id}/disable", userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_ALREADY_DISABLED))
                .andExpect(jsonPath("$.error.message")
                        .value("User already disabled: " + userId));
    }

    @Test
    void shouldReturn409WhenDisableTargetWasAlreadyDeleted() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

        when(userService.disableUser(userId))
                .thenThrow(new UserAlreadyDeletedException(userId));

        mockMvc.perform(patch("/users/{id}/disable", userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_ALREADY_DELETED))
                .andExpect(jsonPath("$.error.message")
                        .value("User already deleted: " + userId));
    }

    @Test
    void shouldSoftDeleteUserById() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userService).softDeleteUser(userId);
    }

    @Test
    void shouldReturn404WhenDeleteTargetDoesNotExist() throws Exception {
        UUID userId = UUID.fromString("70870326-4447-4ef6-a909-2c8dcfd81ba7");

        org.mockito.Mockito.doThrow(new UserNotFoundException(userId))
                .when(userService)
                .softDeleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_NOT_FOUND))
                .andExpect(jsonPath("$.error.message")
                        .value("User not found: " + userId));
    }

    @Test
    void shouldReturn409WhenDeleteTargetWasAlreadyDeleted() throws Exception {
        UUID userId = UUID.fromString("1f3f8e30-8b2d-4c92-9fd8-3f11e50b2031");

        org.mockito.Mockito.doThrow(new UserAlreadyDeletedException(userId))
                .when(userService)
                .softDeleteUser(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(IdentityErrorCode.USER_ALREADY_DELETED))
                .andExpect(jsonPath("$.error.message")
                        .value("User already deleted: " + userId));
    }

    @Test
    void shouldReturn400WhenDeleteUserIdIsNotUuid() throws Exception {
        mockMvc.perform(delete("/users/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code")
                        .value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.message")
                        .value("Request parameter has invalid format"))
                .andExpect(jsonPath("$.error.details.id")
                        .value("must be a valid UUID"));

        verify(userService, never()).softDeleteUser(any());
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
}
