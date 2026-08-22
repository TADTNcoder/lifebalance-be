package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.dto.AssignRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.security.CurrentUser;
import com.lifebalance.identity.service.KeycloakUserMappingService;
import com.lifebalance.identity.service.UserRoleService;

@WebMvcTest(UserRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRoleService userRoleService;

    @MockitoBean
    private KeycloakUserMappingService keycloakUserMappingService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ROLE_ID = UUID.randomUUID();

    @Test
    void shouldGetRolesForUser() throws Exception {
        RoleResponse response = new RoleResponse();
        response.setId(ROLE_ID);

        when(userRoleService.getRoles(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/users/{userId}/roles", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ROLE_ID.toString()));
    }

    @Test
    void shouldAssignRoleToUser() throws Exception {
        AssignRoleRequest request = new AssignRoleRequest();
        request.setRoleId(ROLE_ID);

        // FIX: Giả lập CurrentUser để Controller không bị NullPointerException
        CurrentUser mockUser = mock(CurrentUser.class);
        UUID adminId = UUID.randomUUID();

        // ĐÃ SỬA DÒNG NÀY: Thêm .toString() vì getUserId() trả về String
        when(mockUser.getUserId()).thenReturn(adminId.toString());

        // Trả về mockUser khi mapping Token
        when(keycloakUserMappingService.map(any())).thenReturn(mockUser);

        mockMvc.perform(post("/users/{userId}/roles", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userRoleService).assignRole(eq(USER_ID), any(AssignRoleRequest.class), eq(adminId.toString()));
    }
    @Test
    void shouldRemoveRoleFromUser() throws Exception {
        mockMvc.perform(delete("/users/{userId}/roles/{roleId}", USER_ID, ROLE_ID))
                .andExpect(status().isOk());

        verify(userRoleService).removeRole(eq(USER_ID), eq(ROLE_ID));
    }
}
