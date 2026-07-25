package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.lifebalance.identity.dto.UpdateRoleRequest;
import com.lifebalance.identity.service.RoleService;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @Test
    void shouldGetAllRoles() throws Exception {
        when(roleService.getAll()).thenReturn(List.of(new RoleResponse()));
        mockMvc.perform(get("/roles")).andExpect(status().isOk());
    }

    @Test
    void shouldGetRoleById() throws Exception {
        UUID id = UUID.randomUUID();
        when(roleService.getById(id)).thenReturn(new RoleResponse());
        mockMvc.perform(get("/roles/" + id)).andExpect(status().isOk());
    }

    @Test
    void shouldCreateRole() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode("admin");
        request.setName("Admin Role");
        when(roleService.create(any())).thenReturn(new RoleResponse());

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateRole() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setName("Updated Role");
        when(roleService.update(any(), any())).thenReturn(new RoleResponse());

        mockMvc.perform(put("/roles/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteRole() throws Exception {
        mockMvc.perform(delete("/roles/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}