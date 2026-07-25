package com.lifebalance.identity.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lifebalance.common.api.ModuleStatusResponse;
import com.lifebalance.identity.service.IdentityStatusService;

@WebMvcTest(IdentityStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
class IdentityStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityStatusService statusService;

    @Test
    void shouldReturnIdentityStatus() throws Exception {
        // Đã fix: Truyền 2 tham số chuỗi bất kỳ vào constructor của record
        ModuleStatusResponse mockResponse = new ModuleStatusResponse("identity", "UP");
        when(statusService.status("identity")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/identity/status"))
                .andExpect(status().isOk());
    }
}