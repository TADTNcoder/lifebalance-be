package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.lifebalance.identity.dto.AssignPermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.service.RolePermissionService;

@WebMvcTest(RolePermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class RolePermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RolePermissionService rolePermissionService;

    private static final UUID ROLE_ID = UUID.randomUUID();
    private static final UUID PERMISSION_ID = UUID.randomUUID();

    @Test
    void shouldGetPermissionsForRole() throws Exception {
        PermissionResponse response = new PermissionResponse();
        response.setId(PERMISSION_ID);

        when(rolePermissionService.getPermissions(ROLE_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/roles/{roleId}/permissions", ROLE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PERMISSION_ID.toString()));
    }

    @Test
    void shouldAssignPermissionToRole() throws Exception {
        AssignPermissionRequest request = new AssignPermissionRequest();

        // ĐÃ FIX: Bơm data cho field permissionId để vượt qua ải @Valid
        request.setPermissionId(PERMISSION_ID);

        mockMvc.perform(post("/roles/{roleId}/permissions", ROLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(rolePermissionService).assignPermission(eq(ROLE_ID), any(AssignPermissionRequest.class));
    }

    @Test
    void shouldRemovePermissionFromRole() throws Exception {
        mockMvc.perform(delete("/roles/{roleId}/permissions/{permissionId}", ROLE_ID, PERMISSION_ID))
                .andExpect(status().isOk());

        verify(rolePermissionService).removePermission(ROLE_ID, PERMISSION_ID);
    }
}