package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.RoleSyncResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.service.RoleService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    private static final UUID ROLE_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PERMISSION_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    @Test
    void shouldGetAllRoles() throws Exception {
        when(roleService.getAllRoles()).thenReturn(List.of(roleResponse()));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ROLE_ID.toString()))
                .andExpect(jsonPath("$[0].code").value("MANAGER"));

        verify(roleService).getAllRoles();
    }

    @Test
    void shouldGetRoleById() throws Exception {
        when(roleService.getRoleById(ROLE_ID)).thenReturn(roleResponse());

        mockMvc.perform(get("/roles/{id}", ROLE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROLE_ID.toString()));
    }

    @Test
    void shouldGetRoleByCode() throws Exception {
        when(roleService.getRoleByCode("MANAGER")).thenReturn(roleResponse());

        mockMvc.perform(get("/roles/code/{code}", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MANAGER"));
    }

    @Test
    void shouldCreateRole() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode("MANAGER");
        request.setName("Manager");
        request.setDescription("Manager role");

        when(roleService.createRole(any(CreateRoleRequest.class))).thenReturn(roleResponse());

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROLE_ID.toString()))
                .andExpect(jsonPath("$.code").value("MANAGER"));
    }

    @Test
    void shouldRejectCreateRoleWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSyncAllRolesToKeycloak() throws Exception {
        when(roleService.syncAllRolesToKeycloak()).thenReturn(new RoleSyncResponse(3));

        mockMvc.perform(post("/roles/sync/keycloak"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateRole() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Updated Manager");
        request.setDescription("Updated role");

        when(roleService.updateRole(eq(ROLE_ID), any(UpdateRoleRequest.class)))
                .thenReturn(roleResponse());

        mockMvc.perform(put("/roles/{id}", ROLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROLE_ID.toString()));
    }

    @Test
    void shouldAssignPermissions() throws Exception {
        when(roleService.assignPermissionsToRole(eq(ROLE_ID), any()))
                .thenReturn(roleResponse());

        mockMvc.perform(put("/roles/{id}/permissions", ROLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(PERMISSION_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROLE_ID.toString()));
    }

    @Test
    void shouldDeleteRole() throws Exception {
        doNothing().when(roleService).deleteRole(ROLE_ID);

        mockMvc.perform(delete("/roles/{id}", ROLE_ID))
                .andExpect(status().isOk());

        verify(roleService).deleteRole(ROLE_ID);
    }

    private static RoleResponse roleResponse() {
        RoleResponse response = new RoleResponse();
        response.setId(ROLE_ID);
        response.setCode("MANAGER");
        response.setName("Manager");
        response.setDescription("Manager role");
        response.setSystem(false);
        response.setPermissions(List.of());
        return response;
    }
}