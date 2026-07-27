package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifebalance.identity.model.User;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.AuthorizationService;
import com.lifebalance.identity.service.InternalUserService;
import com.lifebalance.identity.service.KeycloakUserMappingService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakUserMappingService keycloakUserMappingService;

    @MockBean
    private InternalUserService internalUserService;

    @MockBean
    private AuthorizationService authorizationService;

    @Test
    void shouldReturn200WhenCheckingPermission() throws Exception {
        CurrentUser mockCurrentUser = new CurrentUser();
        User mockUser = new User();

        when(keycloakUserMappingService.map(any())).thenReturn(mockCurrentUser);
        when(internalUserService.findOrCreate(any())).thenReturn(mockUser);
        when(authorizationService.checkPermission(any(), any(), any())).thenReturn(null);

        mockMvc.perform(get("/auth/check-permission")
                        .param("permission", "task:read"))
                .andExpect(status().isOk());
    }
}