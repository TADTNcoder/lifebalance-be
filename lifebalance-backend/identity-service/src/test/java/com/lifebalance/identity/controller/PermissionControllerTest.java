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
import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.service.PermissionService;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PermissionService permissionService;

    @Test
    void shouldGetAllPermissions() throws Exception {
        when(permissionService.getAll()).thenReturn(List.of(new PermissionResponse()));
        mockMvc.perform(get("/permissions")).andExpect(status().isOk());
    }

    @Test
    void shouldGetPermissionById() throws Exception {
        UUID id = UUID.randomUUID();
        when(permissionService.getById(id)).thenReturn(new PermissionResponse());
        mockMvc.perform(get("/permissions/" + id)).andExpect(status().isOk());
    }

    @Test
    void shouldCreatePermission() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode("test:read");
        request.setName("Test Read");
        request.setModule("test");
        when(permissionService.create(any())).thenReturn(new PermissionResponse());

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdatePermission() throws Exception {
        UUID id = UUID.randomUUID();
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setCode("test:update");
        request.setName("Updated Test");
        request.setModule("test");
        when(permissionService.update(any(), any())).thenReturn(new PermissionResponse());

        mockMvc.perform(put("/permissions/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeletePermission() throws Exception {
        mockMvc.perform(delete("/permissions/" + UUID.randomUUID())).andExpect(status().isOk());
    }
}