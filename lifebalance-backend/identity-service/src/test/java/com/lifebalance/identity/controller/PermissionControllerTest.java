package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifebalance.identity.dto.CreatePermissionRequest;
import com.lifebalance.identity.dto.PermissionResponse;
import com.lifebalance.identity.dto.UpdatePermissionRequest;
import com.lifebalance.identity.service.PermissionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

    private static final UUID PERMISSION_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    void shouldGetAllPermissions() throws Exception {
        when(permissionService.getAll()).thenReturn(List.of(permissionResponse()));

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PERMISSION_ID.toString()))
                .andExpect(jsonPath("$[0].code").value("task:create"))
                .andExpect(jsonPath("$[0].module").value("task"));

        verify(permissionService).getAll();
    }

    @Test
    void shouldGetPermissionById() throws Exception {
        when(permissionService.getById(PERMISSION_ID)).thenReturn(permissionResponse());

        mockMvc.perform(get("/permissions/{id}", PERMISSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERMISSION_ID.toString()))
                .andExpect(jsonPath("$.code").value("task:create"));

        verify(permissionService).getById(PERMISSION_ID);
    }

    @Test
    @DisplayName("TC_AUTO_05 - Missing module/name/code is rejected with HTTP 400")
    void shouldRejectCreatePermissionWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(permissionService, never()).create(any(CreatePermissionRequest.class));
    }

    @Test
    void shouldCreatePermission() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setModule("task");
        request.setName("Create task");
        request.setCode("task:create");
        request.setDescription("Allows creating tasks");

        when(permissionService.create(any(CreatePermissionRequest.class)))
                .thenReturn(permissionResponse());

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERMISSION_ID.toString()))
                .andExpect(jsonPath("$.code").value("task:create"))
                .andExpect(jsonPath("$.module").value("task"));

        verify(permissionService).create(any(CreatePermissionRequest.class));
    }

    @Test
    void shouldUpdatePermission() throws Exception {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        request.setModule("task");
        request.setName("Create or update task");
        request.setCode("task:create");
        request.setDescription("Updated permission description");

        when(permissionService.update(eq(PERMISSION_ID), any(UpdatePermissionRequest.class)))
                .thenReturn(permissionResponse());

        mockMvc.perform(put("/permissions/{id}", PERMISSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERMISSION_ID.toString()))
                .andExpect(jsonPath("$.code").value("task:create"));

        verify(permissionService).update(eq(PERMISSION_ID), any(UpdatePermissionRequest.class));
    }

    @Test
    void shouldDeletePermission() throws Exception {
        mockMvc.perform(delete("/permissions/{id}", PERMISSION_ID))
                .andExpect(status().isOk());

        verify(permissionService).delete(PERMISSION_ID);
    }

    private static PermissionResponse permissionResponse() {
        PermissionResponse response = new PermissionResponse();
        response.setId(PERMISSION_ID);
        response.setCode("task:create");
        response.setName("Create task");
        response.setModule("task");
        response.setDescription("Allows creating tasks");
        response.setSystem(false);
        return response;
    }
}
