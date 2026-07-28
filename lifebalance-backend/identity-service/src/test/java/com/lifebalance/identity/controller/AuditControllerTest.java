package com.lifebalance.identity.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifebalance.identity.model.AuditLog;
import com.lifebalance.identity.model.enums.AuditAction;
import com.lifebalance.identity.service.AuditLogService;

@WebMvcTest(AuditController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    void shouldReturnAllAuditLogs() throws Exception {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.LOGIN);
        Page<AuditLog> page = new PageImpl<>(List.of(log));

        when(auditLogService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/audit-logs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"));

        verify(auditLogService).getAll(any(Pageable.class));
    }

    @Test
    void shouldReturnAuditLogsByUser() throws Exception {
        UUID userId = UUID.randomUUID();
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.UPDATE_USER); // Đổi Enum theo đúng trong class AuditAction của sếp
        Page<AuditLog> page = new PageImpl<>(List.of(log));

        when(auditLogService.getByUser(eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/audit-logs/user/{userId}", userId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("UPDATE_USER"));

        verify(auditLogService).getByUser(eq(userId), any(Pageable.class));
    }

    @Test
    void shouldReturnAuditLogsByAction() throws Exception {
        AuditLog log = new AuditLog();
        log.setAction(AuditAction.LOGIN);
        Page<AuditLog> page = new PageImpl<>(List.of(log));

        when(auditLogService.getByAction(eq(AuditAction.LOGIN), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/audit-logs/action/{action}", "LOGIN")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("LOGIN"));

        verify(auditLogService).getByAction(eq(AuditAction.LOGIN), any(Pageable.class));
    }
}