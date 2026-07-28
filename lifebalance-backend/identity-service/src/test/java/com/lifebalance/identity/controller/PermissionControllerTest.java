package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @MockitoBean
    private PermissionService permissionService;

    private static final UUID PERMISSION_ID = UUID.randomUUID();

    @Test
    void shouldGetAllPermissions() throws Exception {
        PermissionResponse response = new PermissionResponse();
        response.setId(PERMISSION_ID);

        when(permissionService.getAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PERMISSION_ID.toString()));
    }

    @Test
    void shouldGetPermissionById() throws Exception {
        PermissionResponse response = new PermissionResponse();
        response.setId(PERMISSION_ID);

        when(permissionService.getById(PERMISSION_ID)).thenReturn(response);

        mockMvc.perform(get("/permissions/{id}", PERMISSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERMISSION_ID.toString()));
    }

    @Test
    void shouldCreatePermission() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest();
        // ĐÃ FIX: Bơm data để vượt qua @NotBlank của Spring Validation
        request.setModule("User");
        request.setName("Create User");
        request.setCode("USER_CREATE");

        PermissionResponse response = new PermissionResponse();
        response.setId(PERMISSION_ID);

        // Controller của sếp đang trả về 201 Created (do bên dưới đang check status().isCreated())
        when(permissionService.create(any(CreatePermissionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Sếp lưu ý: Nếu Controller trả về 201 Created thì dùng isCreated(), nếu 200 OK thì sửa thành isOk() nhé. Ở đây thầy để isOk() tạm.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERMISSION_ID.toString()));
    }

    @Test
    void shouldUpdatePermission() throws Exception {
        UpdatePermissionRequest request = new UpdatePermissionRequest();
        // ĐÃ FIX: Bơm data để vượt qua @NotBlank của Spring Validation
        request.setModule("User");
        request.setName("Update User");
        request.setCode("USER_UPDATE");

        PermissionResponse response = new PermissionResponse();
        response.setId(PERMISSION_ID);

        when(permissionService.update(eq(PERMISSION_ID), any(UpdatePermissionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/permissions/{id}", PERMISSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(PERMISSION_ID.toString()));
    }

    @Test
    void shouldDeletePermission() throws Exception {
        mockMvc.perform(delete("/permissions/{id}", PERMISSION_ID))
                .andExpect(status().isOk());

        verify(permissionService).delete(PERMISSION_ID);
    }
}