package com.lifebalance.task.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lifebalance.common.error.AuthErrorCode;
import com.lifebalance.common.error.CommonErrorCode;
import com.lifebalance.common.error.GlobalExceptionHandler;
import com.lifebalance.security.keycloak.KeycloakUserMappingFilter;
import com.lifebalance.security.keycloak.KeycloakUserPrincipal;
import com.lifebalance.task.dto.request.CreateTaskRequest;
import com.lifebalance.task.dto.response.TaskResponse;
import com.lifebalance.task.model.enums.PriorityLevel;
import com.lifebalance.task.model.enums.TaskStatus;
import com.lifebalance.task.service.TaskService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * 10 kịch bản MockMvc dành riêng cho chức năng TẠO TASK.
 *
 * <p>File này dùng standalone MockMvc để kiểm tra Controller + Bean Validation
 * + JSON + GlobalExceptionHandler mà không cần chạy Postman, Keycloak hoặc DB.
 * Service được mock để test ổn định và tập trung đúng contract của API.
 *
 * <p>Mapping Excel:
 * TC_TASK_CREATE_01 ... TC_TASK_CREATE_10.
 */
class TaskCreateApiMockMvcTest {

    private static final String ENDPOINT = "/api/tasks";

    private static final UUID OWNER_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TASK_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID CATEGORY_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private TaskService taskService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        taskService = org.mockito.Mockito.mock(TaskService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TaskController(taskService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("TC_TASK_CREATE_01 - Tạo Task thành công với dữ liệu tối thiểu")
    void tcTaskCreate01_validMinimalPayload_returnsOk() throws Exception {
        when(taskService.create(eq(OWNER_ID), any(CreateTaskRequest.class)))
                .thenReturn(taskResponse("Đọc sách", null, null, null));

        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Đọc sách"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(OWNER_ID.toString()))
                .andExpect(jsonPath("$.name").value("Đọc sách"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        ArgumentCaptor<CreateTaskRequest> captor =
                ArgumentCaptor.forClass(CreateTaskRequest.class);
        verify(taskService).create(eq(OWNER_ID), captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Đọc sách");
    }

    @Test
    @DisplayName("TC_TASK_CREATE_02 - Tạo Task thành công với đầy đủ dữ liệu hợp lệ")
    void tcTaskCreate02_validFullPayload_returnsOkAndMapsAllFields() throws Exception {
        when(taskService.create(eq(OWNER_ID), any(CreateTaskRequest.class)))
                .thenReturn(taskResponse(
                        "Hoàn thành báo cáo",
                        PriorityLevel.HIGH,
                        120,
                        new BigDecimal("250000")));

        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Hoàn thành báo cáo",
                                  "description": "Hoàn thiện báo cáo đồ án",
                                  "note": "Ưu tiên xử lý trước buổi demo",
                                  "currency": "VND",
                                  "priority": "HIGH",
                                  "deadline": "2026-09-04",
                                  "plannedStartAt": "2026-09-03T08:00:00+07:00",
                                  "plannedEndAt": "2026-09-03T10:00:00+07:00",
                                  "estimatedMinutes": 120,
                                  "estimatedCost": 250000,
                                  "categoryId": "%s"
                                }
                                """.formatted(CATEGORY_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hoàn thành báo cáo"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.estimatedMinutes").value(120))
                .andExpect(jsonPath("$.estimatedCost").value(250000));

        ArgumentCaptor<CreateTaskRequest> captor =
                ArgumentCaptor.forClass(CreateTaskRequest.class);
        verify(taskService).create(eq(OWNER_ID), captor.capture());

        CreateTaskRequest request = captor.getValue();
        assertThat(request.getName()).isEqualTo("Hoàn thành báo cáo");
        assertThat(request.getDescription()).isEqualTo("Hoàn thiện báo cáo đồ án");
        assertThat(request.getNote()).isEqualTo("Ưu tiên xử lý trước buổi demo");
        assertThat(request.getCurrency()).isEqualTo("VND");
        assertThat(request.getPriority()).isEqualTo(PriorityLevel.HIGH);
        assertThat(request.getDeadline()).isEqualTo(LocalDate.of(2026, 9, 4));
        assertThat(request.getEstimatedMinutes()).isEqualTo(120);
        assertThat(request.getEstimatedCost()).isEqualByComparingTo("250000");
        assertThat(request.getCategoryId()).isEqualTo(CATEGORY_ID);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_03 - Thiếu trường name trả về 400")
    void tcTaskCreate03_missingName_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "priority": "MEDIUM",
                                  "estimatedMinutes": 60
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.name").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_04 - name chỉ chứa khoảng trắng trả về 400")
    void tcTaskCreate04_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.name").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_05 - name dài hơn 255 ký tự trả về 400")
    void tcTaskCreate05_nameTooLong_returnsBadRequest() throws Exception {
        String tooLongName = "A".repeat(256);

        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(tooLongName)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.name").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_06 - estimatedMinutes = 0 trả về 400")
    void tcTaskCreate06_zeroEstimatedMinutes_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Task thời gian không hợp lệ",
                                  "estimatedMinutes": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.estimatedMinutes").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_07 - estimatedCost âm trả về 400")
    void tcTaskCreate07_negativeEstimatedCost_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Task chi phí không hợp lệ",
                                  "estimatedCost": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.estimatedCost").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_08 - plannedEndAt không sau plannedStartAt trả về 400")
    void tcTaskCreate08_invalidPlanningWindow_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Task có lịch sai",
                                  "plannedStartAt": "2026-09-03T09:00:00+07:00",
                                  "plannedEndAt": "2026-09-03T09:00:00+07:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedEndAt")
                        .value("Planned end time must be after planned start time."));

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_09 - có plannedStartAt nhưng thiếu plannedEndAt trả về 400")
    void tcTaskCreate09_missingPlannedEnd_returnsBadRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .requestAttr(
                                KeycloakUserMappingFilter.CURRENT_USER_ATTRIBUTE,
                                authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Task thiếu giờ kết thúc",
                                  "plannedStartAt": "2026-09-03T09:00:00+07:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(CommonErrorCode.VALIDATION_FAILED))
                .andExpect(jsonPath("$.error.details.plannedEndAt")
                        .value("Planned end time is required when planned start time is provided."));

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("TC_TASK_CREATE_10 - Không có người dùng xác thực trả về 401")
    void tcTaskCreate10_missingAuthenticatedUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Task không có user"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(AuthErrorCode.UNAUTHORIZED));

        verify(taskService, never()).create(any(UUID.class), any(CreateTaskRequest.class));
    }

    private static KeycloakUserPrincipal authenticatedUser() {
        return new KeycloakUserPrincipal(
                "kc-user-1",
                OWNER_ID,
                "lifebalance-user",
                "user@lifebalance.local",
                "LifeBalance User",
                "LifeBalance",
                "User",
                "lifebalance-web",
                Set.of("lifebalance-web"),
                Set.of("user"),
                Set.of("user"),
                Set.of("user")
        );
    }

    private static TaskResponse taskResponse(
            String name,
            PriorityLevel priority,
            Integer estimatedMinutes,
            BigDecimal estimatedCost
    ) {
        TaskResponse response = new TaskResponse();
        response.setId(TASK_ID);
        response.setOwnerId(OWNER_ID);
        response.setUserId(OWNER_ID);
        response.setName(name);
        response.setStatus(TaskStatus.DRAFT);
        response.setPriority(priority);
        response.setEstimatedMinutes(estimatedMinutes);
        response.setEstimatedCost(estimatedCost);
        response.setCategoryId(CATEGORY_ID);
        response.setDeadline(LocalDate.of(2026, 9, 4));
        response.setPlannedStartAt(OffsetDateTime.parse("2026-09-03T08:00:00+07:00"));
        response.setPlannedEndAt(OffsetDateTime.parse("2026-09-03T10:00:00+07:00"));
        return response;
    }
}
