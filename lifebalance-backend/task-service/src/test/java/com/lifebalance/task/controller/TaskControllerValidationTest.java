package com.lifebalance.task.controller;

import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.common.error.GlobalExceptionHandler;
import com.lifebalance.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerValidationTest {

    private final TaskService taskService = mock(TaskService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TaskController(taskService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void createRejectsInvalidPayloadBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Read a chapter",
                                  "estimatedMinutes": 0,
                                  "plannedStartAt": "2026-08-26T09:00:00+07:00",
                                  "plannedEndAt": "2026-08-26T09:00:00+07:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.estimatedMinutes").exists())
                .andExpect(jsonPath("$.error.details.plannedEndAt")
                        .value("Planned end time must be after planned start time."));

        verifyNoInteractions(taskService);
    }
}
