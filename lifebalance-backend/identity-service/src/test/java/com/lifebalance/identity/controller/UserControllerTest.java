package com.lifebalance.identity.controller;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.common.error.GlobalExceptionHandler;
import com.lifebalance.identity.dto.UserResponse;
import com.lifebalance.identity.error.IdentityErrorCode;
import com.lifebalance.identity.exception.UserNotFoundException;
import com.lifebalance.identity.model.enums.AccountStatus;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(
                internalUserService,
                keycloakUserMappingService,
                userService
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
