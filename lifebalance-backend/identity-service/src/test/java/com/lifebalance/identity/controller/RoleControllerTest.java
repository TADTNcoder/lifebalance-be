package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.dto.CreateRoleRequest;
import com.lifebalance.identity.dto.RoleResponse;
import com.lifebalance.identity.dto.RoleSyncResponse;
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.service.RoleService;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false) // Tắt filter bảo mật để test đúng nghiệp vụ Controller
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @Test
    void shouldGetAllRoles() throws Exception {
        // Sửa lại thành getAllRoles() cho khớp với RoleService
        when(roleService.getAllRoles()).thenReturn(List.of(new RoleResponse()));

        mockMvc.perform(get("/roles")).andExpect(status().isOk());
    }

    @Test
    void shouldGetRoleById() throws Exception {
        UUID id = UUID.randomUUID();

        // Sửa lại thành getRoleById()
        when(roleService.getRoleById(id)).thenReturn(new RoleResponse());

        mockMvc.perform(get("/roles/{id}", id)).andExpect(status().isOk());
    }

    // BỔ SUNG: Test cho API lấy Role theo Code
    @Test
    void shouldGetRoleByCode() throws Exception {
        String code = "ADMIN";
        when(roleService.getRoleByCode(code)).thenReturn(new RoleResponse());

        mockMvc.perform(get("/roles/code/{code}", code)).andExpect(status().isOk());
    }

    @Test
    void shouldCreateRole() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode("admin");
        request.setName("Admin Role");

        // Sửa lại thành createRole()
        when(roleService.createRole(any(CreateRoleRequest.class))).thenReturn(new RoleResponse());

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSyncAllRolesToKeycloak() throws Exception {
        when(roleService.syncAllRolesToKeycloak()).thenReturn(new RoleSyncResponse(3));

        mockMvc.perform(post("/roles/sync/keycloak")).andExpect(status().isOk());
    }

    @Test
    void shouldUpdateRole() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Updated Role");

        // Sửa lại thành updateRole()
        when(roleService.updateRole(eq(id), any(UpdateRoleRequest.class))).thenReturn(new RoleResponse());

        mockMvc.perform(put("/roles/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // BỔ SUNG: Test cho API gán Permission vào Role
    @Test
    void shouldAssignPermissions() throws Exception {
        UUID id = UUID.randomUUID();
        List<UUID> permissionIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(roleService.assignPermissionsToRole(eq(id), any())).thenReturn(new RoleResponse());

        mockMvc.perform(put("/roles/{id}/permissions", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionIds)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteRole() throws Exception {
        UUID id = UUID.randomUUID();

        // Sửa lại thành deleteRole()
        doNothing().when(roleService).deleteRole(id);

        mockMvc.perform(delete("/roles/{id}", id)).andExpect(status().isOk());
    }
}